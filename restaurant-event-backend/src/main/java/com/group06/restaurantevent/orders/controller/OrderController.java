package com.group06.restaurantevent.orders.controller;

import com.group06.restaurantevent.orders.dto.request.CreateOrderRequest;
import com.group06.restaurantevent.orders.dto.response.OrderResponse;
import com.group06.restaurantevent.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal UserDetails principal,
                                                     @Valid @RequestBody CreateOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(principal.getUsername(), req));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> myOrders(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(orderService.myOrders(principal.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id,
                                                  @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(orderService.getOrder(id, principal.getUsername()));
    }
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal,
                                               @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(id, principal.getUsername(), request));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(orderService.cancelOrder(id, principal.getUsername()));
    }
}
