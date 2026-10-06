package com.group06.restaurantevent.orders.controller;

import com.group06.restaurantevent.orders.dto.response.OrderResponse;
import com.group06.restaurantevent.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kitchen")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('KITCHEN_STAFF','ADMIN','MANAGER')")
public class KitchenController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> queue() {
        return ResponseEntity.ok(orderService.kitchenQueue());
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                      @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(orderService.updateStatus(id, body.get("status")));
    }
}
