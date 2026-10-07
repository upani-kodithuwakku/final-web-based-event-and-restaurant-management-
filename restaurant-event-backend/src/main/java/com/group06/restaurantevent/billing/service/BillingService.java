package com.group06.restaurantevent.billing.service;

import com.group06.restaurantevent.billing.dto.request.CreateInvoiceRequest;
import com.group06.restaurantevent.billing.dto.request.CreatePaymentRequest;
import com.group06.restaurantevent.billing.dto.response.InvoiceResponse;
import com.group06.restaurantevent.billing.dto.response.PaymentResponse;
import com.group06.restaurantevent.billing.entity.Invoice;
import com.group06.restaurantevent.billing.entity.InvoiceItem;
import com.group06.restaurantevent.billing.entity.Payment;
import com.group06.restaurantevent.billing.repository.InvoiceRepository;
import com.group06.restaurantevent.billing.repository.PaymentRepository;
import com.group06.restaurantevent.common.enums.InvoiceStatus;
import com.group06.restaurantevent.common.enums.InvoiceType;
import com.group06.restaurantevent.common.enums.PaymentMethod;
import com.group06.restaurantevent.common.enums.PaymentStatus;
import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BillingService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    private static final BigDecimal SERVICE_RATE = new BigDecimal("0.10");

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final com.group06.restaurantevent.users.repository.UserRepository users;
    private final com.group06.restaurantevent.orders.repository.FoodOrderRepository orders;
    private final com.group06.restaurantevent.events.repository.EventBookingRepository events;
    private final com.group06.restaurantevent.payment.repository.CustomerPaymentRepository customerPayments;

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest req) {
        InvoiceType type = parseType(req.getInvoiceType());
        var customer = users.findById(req.getCustomerId()).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (customer.getRoles().stream().noneMatch(r -> r.getName().equals("CUSTOMER"))) throw new BadRequestException("Choose a customer account");
        if (!customer.isActive()) throw new BadRequestException("Choose an active customer");
        if ((type == InvoiceType.FOOD_ORDER && (req.getFoodOrderId() == null || req.getEventBookingId() != null))
                || (type == InvoiceType.EVENT_BOOKING && (req.getEventBookingId() == null || req.getFoodOrderId() != null)))
            throw new BadRequestException("Choose exactly one record matching the invoice type");
        BigDecimal amount;
        String description;
        if (type == InvoiceType.FOOD_ORDER) {
            var order = orders.findById(req.getFoodOrderId()).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            if (!order.getCustomerId().equals(req.getCustomerId())) throw new BadRequestException("Order does not belong to this customer");
            if (order.getStatus() == com.group06.restaurantevent.common.enums.OrderStatus.CANCELLED) throw new BadRequestException("Cannot invoice a cancelled order");
            if (invoiceRepository.findByFoodOrderId(order.getId()).isPresent()) throw new com.group06.restaurantevent.common.exception.ConflictException("Order already has an invoice");
            amount = order.getSubtotal(); description = "Food order " + order.getOrderReference();
        } else {
            var booking = events.findById(req.getEventBookingId()).orElseThrow(() -> new ResourceNotFoundException("Event booking not found"));
            if (!booking.getCustomerId().equals(req.getCustomerId())) throw new BadRequestException("Booking does not belong to this customer");
            if (booking.getStatus() != com.group06.restaurantevent.common.enums.EventBookingStatus.CONFIRMED) throw new BadRequestException("Only confirmed events can be invoiced");
            if (invoiceRepository.findByEventBookingId(booking.getId()).isPresent()) throw new com.group06.restaurantevent.common.exception.ConflictException("Event already has an invoice");
            amount = booking.getDepositAmount().divide(new BigDecimal("0.30"), 2, RoundingMode.HALF_UP);
            description = "Event booking " + booking.getBookingReference();
        }
        if (amount == null || amount.signum() <= 0) throw new BadRequestException("Invoice amount must be greater than zero");

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .customerId(req.getCustomerId())
                .foodOrderId(req.getFoodOrderId())
                .eventBookingId(req.getEventBookingId())
                .invoiceType(type)
                .subtotal(BigDecimal.ZERO)
                .serviceCharge(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.ISSUED)
                .issuedAt(LocalDateTime.now())
                .build();

        invoiceRepository.save(invoice);
        return addItemAndRecalculate(invoice.getId(), description, 1, amount);
    }

    @Transactional
    public InvoiceResponse addItemAndRecalculate(Long invoiceId, String description,
                                                  int qty, BigDecimal unitPrice) {
        Invoice invoice = findInvoice(invoiceId);
        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED)
            throw new BadRequestException("Cannot edit a paid or void invoice");
        if (qty < 1 || unitPrice == null || unitPrice.signum() <= 0) throw new BadRequestException("Quantity and price must be positive");
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

        InvoiceItem item = InvoiceItem.builder()
                .invoice(invoice)
                .description(description)
                .quantity(qty)
                .unitPrice(unitPrice)
                .lineTotal(lineTotal)
                .build();
        invoice.getItems().add(item);

        BigDecimal subtotal = invoice.getItems().stream()
                .map(InvoiceItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal svc = subtotal.multiply(SERVICE_RATE).setScale(2, RoundingMode.HALF_UP);

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(tax);
        invoice.setServiceCharge(svc);
        invoice.setTotalAmount(subtotal.add(tax).add(svc).subtract(invoice.getDiscountAmount()));

        return toResponse(invoiceRepository.save(invoice));
    }

    public InvoiceResponse getInvoice(Long id) {
        return toResponse(findInvoice(id));
    }

    public List<InvoiceResponse> myInvoices(Long customerId) {
        return invoiceRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toResponse).toList();
    }

    public List<InvoiceResponse> allInvoices() {
        return invoiceRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public PaymentResponse processPayment(CreatePaymentRequest req) {
        Invoice invoice = findInvoice(req.getInvoiceId());
        if (invoice.getStatus() != InvoiceStatus.ISSUED || invoice.getTotalAmount().signum() <= 0)
            throw new BadRequestException("Only a positive issued invoice can be paid");
        var existing = invoice.getFoodOrderId() != null ? customerPayments.findByFoodOrderId(invoice.getFoodOrderId()) : customerPayments.findByEventBookingId(invoice.getEventBookingId());
        if (existing.filter(p -> p.getStatus() == PaymentStatus.PAID || p.getStatus() == PaymentStatus.PENDING).isPresent())
            throw new com.group06.restaurantevent.common.exception.ConflictException("This booking already has a customer payment; collect that payment instead");

        PaymentMethod method = parseMethod(req.getMethod());

        Payment payment = Payment.builder()
                .paymentReference(generatePaymentRef())
                .invoice(invoice)
                .amount(invoice.getTotalAmount())
                .method(method)
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now(java.time.ZoneId.of("Asia/Colombo")))
                .gatewayReference("SIM-" + System.currentTimeMillis())
                .build();

        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);
        return toPaymentResponse(paymentRepository.save(payment));
    }

    public List<PaymentResponse> getPaymentsForInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceIdOrderByCreatedAtDesc(invoiceId)
                .stream().map(this::toPaymentResponse).toList();
    }

    public InvoiceResponse getInvoiceForUser(Long id, String email, boolean staff) {
        Invoice invoice = findInvoice(id);
        requireOwner(invoice, email, staff);
        return toResponse(invoice);
    }

    public List<PaymentResponse> getPaymentsForUser(Long id, String email, boolean staff) {
        requireOwner(findInvoice(id), email, staff);
        return getPaymentsForInvoice(id);
    }

    public List<InvoiceResponse> myInvoicesForEmail(String email) {
        return myInvoices(users.findByEmailAndIsActiveTrue(email).orElseThrow(() -> new ResourceNotFoundException("Customer not found")).getId());
    }

    @Transactional
    public InvoiceResponse voidInvoice(Long id) {
        Invoice invoice = findInvoice(id);
        if (invoice.getStatus() == InvoiceStatus.PAID) throw new BadRequestException("Paid invoices cannot be voided");
        invoice.setStatus(InvoiceStatus.CANCELLED);
        return toResponse(invoiceRepository.save(invoice));
    }

    private void requireOwner(Invoice invoice, String email, boolean staff) {
        if (!staff && !users.findByEmailAndIsActiveTrue(email).map(u -> u.getId().equals(invoice.getCustomerId())).orElse(false))
            throw new com.group06.restaurantevent.common.exception.ForbiddenException("Access denied");
    }

    private Invoice findInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
    }

    private String generateInvoiceNumber() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "INV-" + ts + "-" + String.format("%04X", new Random().nextInt(0xFFFF));
    }

    private String generatePaymentRef() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "PAY-" + ts + "-" + String.format("%04X", new Random().nextInt(0xFFFF));
    }

    private InvoiceType parseType(String s) {
        try { return InvoiceType.valueOf(s.toUpperCase()); }
        catch (Exception e) { throw new BadRequestException("Invalid invoice type: " + s); }
    }

    private PaymentMethod parseMethod(String s) {
        try { return PaymentMethod.valueOf(s.toUpperCase()); }
        catch (Exception e) { throw new BadRequestException("Invalid payment method: " + s); }
    }

    private InvoiceResponse toResponse(Invoice inv) {
        List<InvoiceResponse.InvoiceItemResponse> items = inv.getItems().stream()
                .map(i -> InvoiceResponse.InvoiceItemResponse.builder()
                        .id(i.getId()).description(i.getDescription())
                        .quantity(i.getQuantity()).unitPrice(i.getUnitPrice())
                        .lineTotal(i.getLineTotal()).build())
                .toList();
        return InvoiceResponse.builder()
                .id(inv.getId()).invoiceNumber(inv.getInvoiceNumber())
                .customerId(inv.getCustomerId())
                .foodOrderId(inv.getFoodOrderId()).eventBookingId(inv.getEventBookingId())
                .invoiceType(inv.getInvoiceType().name())
                .subtotal(inv.getSubtotal()).serviceCharge(inv.getServiceCharge())
                .taxAmount(inv.getTaxAmount()).discountAmount(inv.getDiscountAmount())
                .totalAmount(inv.getTotalAmount()).status(inv.getStatus().name())
                .issuedAt(inv.getIssuedAt()).createdAt(inv.getCreatedAt())
                .items(items).build();
    }

    private PaymentResponse toPaymentResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId()).paymentReference(p.getPaymentReference())
                .invoiceId(p.getInvoice().getId()).amount(p.getAmount())
                .method(p.getMethod().name()).status(p.getStatus().name())
                .paidAt(p.getPaidAt()).createdAt(p.getCreatedAt()).build();
    }
}
