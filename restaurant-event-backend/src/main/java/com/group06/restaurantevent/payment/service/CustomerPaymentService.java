package com.group06.restaurantevent.payment.service;

import com.group06.restaurantevent.common.audit.AuditLog;
import com.group06.restaurantevent.common.audit.AuditLogRepository;
import com.group06.restaurantevent.common.enums.EventBookingStatus;
import com.group06.restaurantevent.common.enums.OrderStatus;
import com.group06.restaurantevent.common.enums.PaymentOption;
import com.group06.restaurantevent.common.enums.PaymentPurpose;
import com.group06.restaurantevent.common.enums.PaymentStatus;
import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.common.exception.ConflictException;
import com.group06.restaurantevent.common.exception.ForbiddenException;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import com.group06.restaurantevent.events.entity.EventBooking;
import com.group06.restaurantevent.events.repository.EventBookingRepository;
import com.group06.restaurantevent.orders.entity.FoodOrder;
import com.group06.restaurantevent.orders.repository.FoodOrderRepository;
import com.group06.restaurantevent.payment.dto.request.CreateCustomerPaymentRequest;
import com.group06.restaurantevent.payment.dto.request.UpdateCustomerPaymentRequest;
import com.group06.restaurantevent.payment.dto.request.UpdatePaymentStatusRequest;
import com.group06.restaurantevent.payment.dto.response.CustomerPaymentResponse;
import com.group06.restaurantevent.payment.dto.response.PaymentSummaryResponse;
import com.group06.restaurantevent.payment.dto.response.PaymentSummaryResponse.BillLine;
import com.group06.restaurantevent.payment.entity.CustomerPayment;
import com.group06.restaurantevent.payment.repository.CustomerPaymentRepository;
import com.group06.restaurantevent.payment.strategy.PaymentMethodStrategy;
import com.group06.restaurantevent.users.entity.User;
import com.group06.restaurantevent.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.group06.restaurantevent.reservations.entity.TableReservation;
import com.group06.restaurantevent.reservations.repository.TableReservationRepository;
import com.group06.restaurantevent.common.enums.ReservationStatus;
import java.time.ZoneId;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CustomerPaymentService {

    /** Matches the service charge shown to customers in the menu bag. */
    static final BigDecimal SERVICE_RATE = new BigDecimal("0.10");

    /** A food order counts as confirmed once the kitchen accepts it (it leaves PENDING).
     *  Customers may pay for food at checkout, before that confirmation. */
    private static final List<OrderStatus> CONFIRMED_ORDER_STATUSES =
            List.of(OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.SERVED, OrderStatus.COMPLETED);

    private final CustomerPaymentRepository paymentRepository;
    private final FoodOrderRepository orderRepository;
    private final EventBookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final Map<PaymentOption, PaymentMethodStrategy> strategies;
    private final TableReservationRepository reservationRepository;
    private final BigDecimal depositPerGuest;

    public CustomerPaymentService(CustomerPaymentRepository paymentRepository,
                                  FoodOrderRepository orderRepository,
                                  EventBookingRepository bookingRepository,
                                  UserRepository userRepository,
                                  AuditLogRepository auditLogRepository,
                                  List<PaymentMethodStrategy> strategyList,
                                  TableReservationRepository reservationRepository,
                                  @Value("${app.reservation.deposit-per-guest:500}") BigDecimal depositPerGuest) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.strategies = new EnumMap<>(PaymentOption.class);
        for (PaymentMethodStrategy strategy : strategyList) {
            if (strategies.putIfAbsent(strategy.option(), strategy) != null)
                throw new IllegalStateException("Duplicate payment strategy: " + strategy.option());
        }
        this.reservationRepository = reservationRepository;
        this.depositPerGuest = depositPerGuest.setScale(2, RoundingMode.HALF_UP);
    }

    // ── Create ────────────────────────────────────────────────

    @Transactional
    public CustomerPaymentResponse create(String customerEmail, CreateCustomerPaymentRequest req) {
        Long customerId = findUserByEmail(customerEmail).getId();
        boolean forOrder = req.getFoodOrderId() != null;
        int targets = (forOrder ? 1 : 0) + (req.getEventBookingId() != null ? 1 : 0) + (req.getTableReservationId() != null ? 1 : 0);
        if (targets != 1) throw new BadRequestException("Choose exactly one food order, event booking or table reservation");

        CustomerPayment payment = CustomerPayment.builder()
                .paymentReference(generateReference())
                .customerId(customerId)
                .build();

        if (forOrder) {
            FoodOrder order = findOrder(req.getFoodOrderId());
            requireOwner(order.getCustomerId(), customerId);
            if (order.getStatus() == OrderStatus.CANCELLED)
                throw new ConflictException("This order was cancelled");
            if (paymentRepository.existsByFoodOrderId(order.getId()))
                throw new ConflictException("A payment already exists for this order");
            payment.setPurpose(PaymentPurpose.FOOD_ORDER);
            payment.setFoodOrderId(order.getId());
            payment.setAmount(foodTotal(order));
        } else if (req.getTableReservationId() != null) {
            TableReservation reservation = findReservation(req.getTableReservationId());
            requireOwner(reservation.getCustomer().getId(), customerId);
            requirePayableReservation(reservation);
            if (paymentRepository.existsByTableReservationId(reservation.getId()))
                throw new ConflictException("A payment already exists for this reservation");
            payment.setPurpose(PaymentPurpose.TABLE_RESERVATION);
            payment.setTableReservationId(reservation.getId());
            payment.setAmount(reservationTotal(reservation));
        } else {
            EventBooking booking = findBooking(req.getEventBookingId());
            requireOwner(booking.getCustomerId(), customerId);
            if (booking.getStatus() != EventBookingStatus.CONFIRMED)
                throw new ConflictException("This event booking has not been confirmed by our staff yet");
            if (paymentRepository.existsByEventBookingId(booking.getId()))
                throw new ConflictException("A payment already exists for this event booking");
            payment.setPurpose(PaymentPurpose.EVENT_BOOKING);
            payment.setEventBookingId(booking.getId());
            payment.setAmount(eventTotal(booking));
        }

        strategyFor(req.getMethod()).apply(payment, req.getCard());
        CustomerPayment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CustomerPaymentResponse> myPayments(String customerEmail) {
        return paymentRepository.findByCustomerIdOrderByCreatedAtDesc(findUserByEmail(customerEmail).getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerPaymentResponse> allPayments(String status) {
        List<CustomerPayment> payments = status == null || status.isBlank()
                ? paymentRepository.findAllByOrderByCreatedAtDesc()
                : paymentRepository.findByStatusOrderByCreatedAtDesc(parseStatus(status));
        return payments.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomerPaymentResponse getPayment(Long id, String email, boolean staffView) {
        CustomerPayment payment = findPayment(id);
        if (!staffView) requireOwner(payment.getCustomerId(), findUserByEmail(email).getId());
        return toResponse(payment);
    }

    /** The customer's bill, with food, event and reservation totals kept separate. */
    @Transactional(readOnly = true)
    public PaymentSummaryResponse summary(String customerEmail) {
        Long customerId = findUserByEmail(customerEmail).getId();
        List<CustomerPayment> payments = paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        Map<Long, CustomerPayment> byOrder = indexBy(payments, CustomerPayment::getFoodOrderId);
        Map<Long, CustomerPayment> byBooking = indexBy(payments, CustomerPayment::getEventBookingId);

        List<BillLine> food = new ArrayList<>();
        for (FoodOrder order : orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)) {
            CustomerPayment payment = byOrder.get(order.getId());
            // Cancelled orders only appear if money was involved (e.g. a refund).
            if (order.getStatus() == OrderStatus.CANCELLED && payment == null) continue;
            BigDecimal service = serviceCharge(order.getSubtotal());
            food.add(withPayment(BillLine.builder()
                    .purpose(PaymentPurpose.FOOD_ORDER.name())
                    .targetId(order.getId())
                    .reference(order.getOrderReference())
                    .description(order.getItems().stream()
                            .map(i -> i.getQuantity() + " × " + i.getItemNameSnapshot())
                            .collect(Collectors.joining(", ")))
                    .createdAt(order.getCreatedAt())
                    .targetStatus(order.getStatus().name())
                    .confirmed(CONFIRMED_ORDER_STATUSES.contains(order.getStatus()))
                    .payable(order.getStatus() != OrderStatus.CANCELLED)
                    .subtotal(order.getSubtotal())
                    .serviceCharge(service)
                    .total(order.getSubtotal().add(service))
                    .items(order.getItems().stream().map(i -> PaymentSummaryResponse.BillItem.builder()
                            .name(i.getItemNameSnapshot()).quantity(i.getQuantity())
                            .unitPrice(i.getUnitPriceSnapshot()).lineTotal(i.getLineTotal()).build()).toList()), payment));
        }

        List<BillLine> events = new ArrayList<>();
        for (EventBooking booking : bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)) {
            CustomerPayment payment = byBooking.get(booking.getId());
            boolean closed = booking.getStatus() == EventBookingStatus.REJECTED
                    || booking.getStatus() == EventBookingStatus.CANCELLED;
            if (closed && payment == null) continue;
            BigDecimal total = eventTotal(booking);
            events.add(withPayment(BillLine.builder()
                    .purpose(PaymentPurpose.EVENT_BOOKING.name())
                    .targetId(booking.getId())
                    .reference(booking.getBookingReference())
                    .description(booking.getEventPackage().getName() + " · " + booking.getHall().getName()
                            + " · " + booking.getGuestCount() + " guests")
                    .eventDate(booking.getEventDate())
                    .createdAt(booking.getCreatedAt())
                    .targetStatus(booking.getStatus().name())
                    .confirmed(booking.getStatus() == EventBookingStatus.CONFIRMED)
                    .payable(booking.getStatus() == EventBookingStatus.CONFIRMED)
                    .subtotal(total)
                    .serviceCharge(BigDecimal.ZERO.setScale(2))
                    .total(total)
                    .items(List.of(PaymentSummaryResponse.BillItem.builder()
                            .name(booking.getEventPackage().getName() + " (" + booking.getGuestCount() + " guests)")
                            .quantity(1).unitPrice(total).lineTotal(total).build())), payment));
        }

        Map<Long, CustomerPayment> byReservation = indexBy(payments, CustomerPayment::getTableReservationId);
        List<BillLine> reservations = new ArrayList<>();
        for (TableReservation reservation : reservationRepository.findByCustomerIdOrderByReservationDateDescCreatedAtDesc(customerId)) {
            CustomerPayment payment = byReservation.get(reservation.getId());
            boolean payable = reservationPayable(reservation);
            if (!payable && payment == null) continue;
            BigDecimal total = payment != null ? payment.getAmount() : reservationTotal(reservation);
            reservations.add(withPayment(BillLine.builder()
                    .purpose(PaymentPurpose.TABLE_RESERVATION.name()).targetId(reservation.getId())
                    .reference(reservation.getBookingReference())
                    .description(reservation.getTable().getTableNumber() + " · " + reservation.getGuestCount() + " guests · " + reservation.getStartTime())
                    .eventDate(reservation.getReservationDate()).createdAt(reservation.getCreatedAt())
                    .targetStatus(reservation.getStatus().name()).confirmed(reservation.getStatus() == ReservationStatus.CONFIRMED)
                    .payable(payable).subtotal(total).serviceCharge(BigDecimal.ZERO.setScale(2)).total(total)
                    .items(List.of(PaymentSummaryResponse.BillItem.builder().name("Reservation deposit")
                            .quantity(reservation.getGuestCount()).unitPrice(total.divide(BigDecimal.valueOf(reservation.getGuestCount()), 2, RoundingMode.HALF_UP)).lineTotal(total).build())), payment));
        }
        BigDecimal reservationTotal = payableTotal(reservations);
        BigDecimal foodTotal = payableTotal(food);
        BigDecimal eventTotal = payableTotal(events);
        BigDecimal grandTotal = foodTotal == null && eventTotal == null && reservationTotal == null ? null
                : Objects.requireNonNullElse(foodTotal, BigDecimal.ZERO)
                        .add(Objects.requireNonNullElse(eventTotal, BigDecimal.ZERO))
                        .add(Objects.requireNonNullElse(reservationTotal, BigDecimal.ZERO));
        BigDecimal paid = paidTotal(food).add(paidTotal(events)).add(paidTotal(reservations));

        return PaymentSummaryResponse.builder()
                .tableReservations(reservations).reservationTotal(reservationTotal).depositPerGuest(depositPerGuest)
                .foodOrders(food)
                .eventBookings(events)
                .foodTotal(foodTotal)
                .eventTotal(eventTotal)
                .grandTotal(grandTotal)
                .amountPaid(paid)
                .amountDue(grandTotal == null ? BigDecimal.ZERO.setScale(2) : grandTotal.subtract(paid).max(BigDecimal.ZERO))
                .build();
    }

    // ── Update ────────────────────────────────────────────────

    /** Customer changes how they pay, e.g. from paying at the outlet to paying by card now. */
    @Transactional
    public CustomerPaymentResponse updateMethod(Long id, String customerEmail, UpdateCustomerPaymentRequest req) {
        CustomerPayment payment = findPayment(id);
        requireOwner(payment.getCustomerId(), findUserByEmail(customerEmail).getId());
        if (payment.getStatus() == PaymentStatus.PAID || payment.getStatus() == PaymentStatus.REFUNDED)
            throw new ConflictException("This payment is already " + payment.getStatus().name().toLowerCase());
        validateBookingPayment(payment);
        strategyFor(req.getMethod()).apply(payment, req.getCard());
        CustomerPayment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    /** Staff update the status, e.g. marking a pay-at-outlet payment as collected. */
    @Transactional
    public CustomerPaymentResponse updateStatus(Long id, String staffEmail, UpdatePaymentStatusRequest req) {
        CustomerPayment payment = findPayment(id);
        PaymentStatus current = payment.getStatus();
        PaymentStatus next = parseStatus(req.getStatus());
        boolean allowed = switch (current) {
            case PENDING -> next == PaymentStatus.PAID || next == PaymentStatus.FAILED;
            case PAID -> next == PaymentStatus.REFUNDED;
            case FAILED -> next == PaymentStatus.PENDING;
            case REFUNDED -> false;
        };
        if (!allowed)
            throw new BadRequestException("Cannot change payment from " + current + " to " + next);

        if (next == PaymentStatus.PAID) validateBookingPayment(payment);
        payment.setStatus(next);
        if (next == PaymentStatus.PAID) payment.setPaidAt(LocalDateTime.now(ZoneId.of("Asia/Colombo")));
        if (next == PaymentStatus.PENDING) payment.setPaidAt(null);
        CustomerPayment saved = paymentRepository.save(payment);
        CustomerPaymentResponse response = toResponse(saved);
        audit(staffEmail, "PAYMENT_STATUS_UPDATED", payment.getId(), current.name(), next.name());
        return response;
    }

    // ── Delete ────────────────────────────────────────────────

    @Transactional
    public void delete(Long id, String adminEmail) {
        CustomerPayment payment = findPayment(id);
        paymentRepository.delete(payment);
        audit(adminEmail, "PAYMENT_DELETED", payment.getId(),
                payment.getPaymentReference() + " " + payment.getStatus() + " " + payment.getAmount(), null);
    }

    // ── Helpers ───────────────────────────────────────────────

    private String targetReference(CustomerPayment p) {
        return switch (p.getPurpose()) {
            case FOOD_ORDER -> orderRepository.findById(p.getFoodOrderId()).map(FoodOrder::getOrderReference).orElse("your order");
            case EVENT_BOOKING -> bookingRepository.findById(p.getEventBookingId()).map(EventBooking::getBookingReference).orElse("your booking");
            case TABLE_RESERVATION -> reservationRepository.findById(p.getTableReservationId()).map(TableReservation::getBookingReference).orElse("your reservation");
        };
    }

    private String receiptMessage(CustomerPayment p) {
        String type = switch (p.getPurpose()) {
            case FOOD_ORDER -> "food order ";
            case EVENT_BOOKING -> "your event booking ";
            case TABLE_RESERVATION -> "your table reservation ";
        };
        return String.format(java.util.Locale.US, "Gather: Payment successful! LKR %,.2f received for %s%s (ref %s). Thank you - gather.com",
                p.getAmount(), type, targetReference(p), p.getPaymentReference());
    }

    private TableReservation findReservation(Long id) {
        return reservationRepository.findForPayment(id).orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));
    }

    private boolean reservationPayable(TableReservation reservation) {
        return (reservation.getStatus() == ReservationStatus.PENDING || reservation.getStatus() == ReservationStatus.CONFIRMED)
                && LocalDateTime.of(reservation.getReservationDate(), reservation.getStartTime()).isAfter(LocalDateTime.now(ZoneId.of("Asia/Colombo")));
    }

    private void requirePayableReservation(TableReservation reservation) {
        if (!reservationPayable(reservation)) throw new ConflictException("Only future pending or confirmed reservations are payable");
    }

    private BigDecimal reservationTotal(TableReservation reservation) {
        return depositPerGuest.multiply(BigDecimal.valueOf(reservation.getGuestCount())).setScale(2, RoundingMode.HALF_UP);
    }

    private void validateBookingPayment(CustomerPayment payment) {
        if (payment.getEventBookingId() != null && findBooking(payment.getEventBookingId()).getStatus() != EventBookingStatus.CONFIRMED) {
            throw new ConflictException("Only confirmed event bookings are payable");
        }
        if (payment.getTableReservationId() != null) {
            TableReservation reservation = findReservation(payment.getTableReservationId());
            requirePayableReservation(reservation);
            // Keep a pending deposit aligned with any change to the party size.
            payment.setAmount(reservationTotal(reservation));
        }
    }

    @Transactional
    public void cancelReservationPayment(Long reservationId, String email) {
        paymentRepository.findByTableReservationId(reservationId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.PAID) {
                payment.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
                audit(email, "RESERVATION_DEPOSIT_REFUNDED", payment.getId(), "PAID", "REFUNDED");
            } else if (payment.getStatus() == PaymentStatus.PENDING && payment.getMethod() == PaymentOption.PAY_AT_OUTLET) {
                paymentRepository.delete(payment);
                audit(email, "RESERVATION_OUTLET_PAYMENT_CANCELLED", payment.getId(), "PENDING", null);
            }
        });
    }

    @Transactional(readOnly = true)
    public List<CustomerPaymentResponse> paymentsForPurpose(PaymentPurpose purpose) {
        return paymentRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(p -> p.getPurpose() == purpose).map(this::toResponse).toList();
    }


    private BigDecimal serviceCharge(BigDecimal subtotal) {
        return subtotal.multiply(SERVICE_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal foodTotal(FoodOrder order) {
        return order.getSubtotal().add(serviceCharge(order.getSubtotal()));
    }

    private BigDecimal eventTotal(EventBooking booking) {
        return booking.getEventPackage().getBasePrice().setScale(2, RoundingMode.HALF_UP);
    }

    private BillLine withPayment(BillLine.BillLineBuilder line, CustomerPayment payment) {
        if (payment != null) {
            line.paymentId(payment.getId())
                    .paymentReference(payment.getPaymentReference())
                    .paymentMethod(payment.getMethod().name())
                    .paymentStatus(payment.getStatus().name())
                    .cardLast4(payment.getCardLast4());
        }
        return line.build();
    }

    /** Sum of payable lines, or null when there are none. */
    private BigDecimal payableTotal(List<BillLine> lines) {
        return lines.stream().filter(BillLine::isPayable).map(BillLine::getTotal)
                .reduce(BigDecimal::add).orElse(null);
    }

    private BigDecimal paidTotal(List<BillLine> lines) {
        return lines.stream()
                .filter(l -> l.isPayable() && PaymentStatus.PAID.name().equals(l.getPaymentStatus()))
                .map(BillLine::getTotal).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    private Map<Long, CustomerPayment> indexBy(List<CustomerPayment> payments, Function<CustomerPayment, Long> key) {
        return payments.stream().filter(p -> key.apply(p) != null)
                .collect(Collectors.toMap(key, Function.identity(), (a, b) -> a));
    }

    private PaymentMethodStrategy strategyFor(String method) {
        PaymentOption option;
        try { option = PaymentOption.valueOf(method.trim().toUpperCase()); }
        catch (Exception e) { throw new BadRequestException("Invalid payment method: " + method); }
        PaymentMethodStrategy strategy = strategies.get(option);
        if (strategy == null) throw new BadRequestException("Payment method is not supported: " + method);
        return strategy;
    }

    private PaymentStatus parseStatus(String status) {
        try { return PaymentStatus.valueOf(status.trim().toUpperCase()); }
        catch (Exception e) { throw new BadRequestException("Invalid payment status: " + status); }
    }

    private void requireOwner(Long ownerId, Long customerId) {
        if (!ownerId.equals(customerId)) throw new ForbiddenException("Access denied");
    }

    private void audit(String email, String action, Long entityId, String oldValue, String newValue) {
        Long userId = userRepository.findByEmailAndIsActiveTrue(email).map(User::getId).orElse(null);
        auditLogRepository.save(AuditLog.builder()
                .userId(userId).action(action).entityName("CustomerPayment").entityId(entityId)
                .oldValue(oldValue).newValue(newValue).build());
    }

    private CustomerPayment findPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    private FoodOrder findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    private EventBooking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event booking not found: " + id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private String generateReference() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "PMT-" + date + "-" + String.format("%04X", new Random().nextInt(0xFFFF));
    }

    private CustomerPaymentResponse toResponse(CustomerPayment p) {
        String targetReference = targetReference(p);
        return CustomerPaymentResponse.builder()
                .id(p.getId())
                .paymentReference(p.getPaymentReference())
                .customerId(p.getCustomerId())
                .purpose(p.getPurpose().name())
                .foodOrderId(p.getFoodOrderId())
                .eventBookingId(p.getEventBookingId()).tableReservationId(p.getTableReservationId())
                .targetReference(targetReference)
                .amount(p.getAmount())
                .method(p.getMethod().name())
                .status(p.getStatus().name())
                .cardHolderName(p.getCardHolderName())
                .cardLast4(p.getCardLast4())
                .cardBrand(p.getCardBrand())
                .gatewayReference(p.getGatewayReference())
                .confirmationMessage(p.getStatus() == PaymentStatus.PAID ? receiptMessage(p) : null)
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
