package com.group06.restaurantevent.orders.service;

import com.group06.restaurantevent.common.enums.OrderStatus;
import com.group06.restaurantevent.common.enums.OrderType;
import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.common.exception.ConflictException;
import com.group06.restaurantevent.common.exception.ForbiddenException;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import com.group06.restaurantevent.menu.entity.MenuItem;
import com.group06.restaurantevent.menu.service.MenuService;
import com.group06.restaurantevent.orders.dto.request.CreateOrderRequest;
import com.group06.restaurantevent.orders.dto.response.OrderItemResponse;
import com.group06.restaurantevent.orders.dto.response.OrderResponse;
import com.group06.restaurantevent.orders.entity.FoodOrder;
import com.group06.restaurantevent.orders.entity.FoodOrderItem;
import com.group06.restaurantevent.orders.repository.FoodOrderRepository;
import com.group06.restaurantevent.users.entity.User;
import com.group06.restaurantevent.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final FoodOrderRepository orderRepository;
    private final MenuService menuService;
    private final UserRepository userRepository;
    private final com.group06.restaurantevent.reservations.repository.RestaurantTableRepository tables;
    private final com.group06.restaurantevent.reservations.repository.TableReservationRepository reservations;
    private final com.group06.restaurantevent.payment.repository.CustomerPaymentRepository payments;
    private final com.group06.restaurantevent.billing.repository.InvoiceRepository invoices;

    @Transactional
    public OrderResponse createOrder(String customerEmail, CreateOrderRequest req) {
        Long customerId = findUserByEmail(customerEmail).getId();
        if (req.getItems() == null || req.getItems().isEmpty())
            throw new BadRequestException("At least one item is required");

        validateReferences(customerId, req);
        FoodOrder order = FoodOrder.builder()
                .orderReference(generateRef())
                .customerId(customerId)
                .tableId(req.getTableId())
                .reservationId(req.getReservationId())
                .orderType(parseType(req.getOrderType()))
                .status(OrderStatus.PENDING)
                .specialNote(req.getSpecialNote())
                .subtotal(BigDecimal.ZERO)
                .build();

        fillItems(order, req);
        return toResponse(orderRepository.save(order));
    }

    private void fillItems(FoodOrder order, CreateOrderRequest req) {
        BigDecimal subtotal = BigDecimal.ZERO;
        if (req.getItems() == null || req.getItems().isEmpty()) throw new BadRequestException("At least one item is required");
        order.getItems().clear();
        for (CreateOrderRequest.OrderItemRequest ir : req.getItems()) {
            if (ir.getQuantity() == null || ir.getQuantity() < 1)
                throw new BadRequestException("Quantity must be at least 1");

            MenuItem menuItem = menuService.findItem(ir.getMenuItemId());
            if (!menuItem.isAvailable() || !menuItem.isActive())
                throw new ConflictException("Menu item '" + menuItem.getName() + "' is currently unavailable");

            BigDecimal lineTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(ir.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            FoodOrderItem item = FoodOrderItem.builder()
                    .order(order)
                    .menuItemId(menuItem.getId())
                    .itemNameSnapshot(menuItem.getName())
                    .unitPriceSnapshot(menuItem.getPrice())
                    .quantity(ir.getQuantity())
                    .specialNote(ir.getSpecialNote())
                    .lineTotal(lineTotal)
                    .build();
            order.getItems().add(item);
        }

        order.setSubtotal(subtotal);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> myOrders(String customerEmail) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(findUserByEmail(customerEmail).getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id, String customerEmail) {
        FoodOrder order = findOrder(id);
        if (!order.getCustomerId().equals(findUserByEmail(customerEmail).getId()))
            throw new ForbiddenException("Access denied");
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> kitchenQueue() {
        return orderRepository.findByStatusInOrderByCreatedAtAsc(
                List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY))
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, String statusStr) {
        FoodOrder order = findOrder(id);
        OrderStatus newStatus = parseStatus(statusStr);
        validateTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        return toResponse(orderRepository.save(order));
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING -> next == OrderStatus.READY || next == OrderStatus.CANCELLED;
            case READY -> next == OrderStatus.SERVED;
            case SERVED -> next == OrderStatus.COMPLETED;
            default -> false;
        };
        if (!valid)
            throw new BadRequestException("Cannot transition from " + current + " to " + next);
    }

    @Transactional
    public OrderResponse updateOrder(Long id, String email, CreateOrderRequest request) {
        FoodOrder order = ownedPending(id, email);
        if (payments.existsByFoodOrderId(id) || invoices.findByFoodOrderId(id).filter(i -> i.getStatus() != com.group06.restaurantevent.common.enums.InvoiceStatus.CANCELLED).isPresent())
            throw new ConflictException("Orders with a payment or invoice cannot be edited");
        validateReferences(order.getCustomerId(), request);
        order.setTableId(request.getTableId()); order.setReservationId(request.getReservationId());
        order.setOrderType(parseType(request.getOrderType())); order.setSpecialNote(request.getSpecialNote());
        fillItems(order, request);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long id, String email) {
        FoodOrder order = ownedPending(id, email);
        if (payments.existsByFoodOrderId(id) || invoices.findByFoodOrderId(id).filter(i -> i.getStatus() != com.group06.restaurantevent.common.enums.InvoiceStatus.CANCELLED).isPresent())
            throw new ConflictException("Contact staff to cancel an order with a payment or invoice");
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    private FoodOrder ownedPending(Long id, String email) {
        FoodOrder order = findOrder(id);
        if (!order.getCustomerId().equals(findUserByEmail(email).getId())) throw new ForbiddenException("Access denied");
        if (order.getStatus() != OrderStatus.PENDING) throw new ConflictException("Only pending orders can be edited or cancelled");
        return order;
    }

    private void validateReferences(Long customerId, CreateOrderRequest request) {
        if (request.getTableId() != null) tables.findByIdAndIsActiveTrue(request.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
        if (request.getReservationId() != null) {
            var reservation = reservations.findById(request.getReservationId()).orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
            if (!reservation.getCustomer().getId().equals(customerId)) throw new ForbiddenException("Reservation does not belong to you");
            if (!java.util.Set.of(com.group06.restaurantevent.common.enums.ReservationStatus.PENDING,
                    com.group06.restaurantevent.common.enums.ReservationStatus.CONFIRMED,
                    com.group06.restaurantevent.common.enums.ReservationStatus.CHECKED_IN).contains(reservation.getStatus()))
                throw new ConflictException("Reservation is no longer active");
            if (request.getTableId() != null && !reservation.getTable().getId().equals(request.getTableId()))
                throw new BadRequestException("Table does not match the reservation");
            request.setTableId(reservation.getTable().getId());
        }
        if (parseType(request.getOrderType()) == OrderType.TAKEAWAY && (request.getTableId() != null || request.getReservationId() != null))
            throw new BadRequestException("Takeaway orders cannot use a table or reservation");
    }

    private FoodOrder findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private String generateRef() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = String.format("%04X", new Random().nextInt(0xFFFF));
        return "ORD-" + ts + "-" + suffix;
    }

    private OrderType parseType(String s) {
        try { return OrderType.valueOf(s.toUpperCase()); }
        catch (Exception e) { throw new BadRequestException("Invalid order type"); }
    }

    private OrderStatus parseStatus(String s) {
        try { return OrderStatus.valueOf(s.toUpperCase()); }
        catch (Exception e) { throw new BadRequestException("Invalid status: " + s); }
    }

    public OrderResponse toResponse(FoodOrder o) {
        return OrderResponse.builder()
                .id(o.getId())
                .orderReference(o.getOrderReference())
                .customerId(o.getCustomerId())
                .tableId(o.getTableId())
                .reservationId(o.getReservationId())
                .orderType(o.getOrderType().name())
                .status(o.getStatus().name())
                .specialNote(o.getSpecialNote())
                .subtotal(o.getSubtotal())
                .items(o.getItems().stream().map(this::toItemResponse).toList())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    private OrderItemResponse toItemResponse(FoodOrderItem i) {
        return OrderItemResponse.builder()
                .id(i.getId())
                .menuItemId(i.getMenuItemId())
                .itemNameSnapshot(i.getItemNameSnapshot())
                .unitPriceSnapshot(i.getUnitPriceSnapshot())
                .quantity(i.getQuantity())
                .specialNote(i.getSpecialNote())
                .lineTotal(i.getLineTotal())
                .build();
    }
}
