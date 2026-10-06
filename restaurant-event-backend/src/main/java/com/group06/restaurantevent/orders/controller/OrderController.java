package com.group06.restaurantevent.orders.controller;

import com.group06.restaurantevent.orders.dto.request.CreateOrderRequest;
import com.group06.restaurantevent.orders.dto.response.OrderResponse;
import com.group06.restaurantevent.orders.service.OrderService;
import com.group06.restaurantevent.users.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal User user,
                                                     @Valid @RequestBody CreateOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(user.getId(), req));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> myOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.myOrders(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id,
                                                  @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getOrder(id, user.getId()));
    }
}
