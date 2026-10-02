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

    @Transactional
    public OrderResponse createOrder(Long customerId, CreateOrderRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty())
            throw new BadRequestException("At least one item is required");

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

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest ir : req.getItems()) {
            if (ir.getQuantity() == null || ir.getQuantity() < 1)
                throw new BadRequestException("Quantity must be at least 1");

            MenuItem menuItem = menuService.findItem(ir.getMenuItemId());
            if (!menuItem.isAvailable())
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
        return toResponse(orderRepository.save(order));
    }

    public List<OrderResponse> myOrders(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toResponse).toList();
    }

    public OrderResponse getOrder(Long id, Long customerId) {
        FoodOrder order = findOrder(id);
        if (!order.getCustomerId().equals(customerId))
            throw new ForbiddenException("Access denied");
        return toResponse(order);
    }

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

    private FoodOrder findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    private String generateRef() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = String.format("%04X", new Random().nextInt(0xFFFF));
        return "ORD-" + ts + "-" + suffix;
    }

    private OrderType parseType(String s) {
        try { return OrderType.valueOf(s.toUpperCase()); }
        catch (Exception e) { return OrderType.DINE_IN; }
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
