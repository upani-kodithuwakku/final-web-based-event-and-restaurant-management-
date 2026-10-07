package com.group06.restaurantevent.inventory.controller;

import com.group06.restaurantevent.inventory.dto.request.PurchaseOrderRequest;
import com.group06.restaurantevent.inventory.dto.response.PurchaseOrderResponse;
import com.group06.restaurantevent.inventory.service.PurchaseOrderService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/purchase-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER','INVENTORY_MANAGER')")
public class PurchaseOrderController {
    private final PurchaseOrderService service;

    @GetMapping
    public List<PurchaseOrderResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse create(@Valid @RequestBody PurchaseOrderRequest request) {
        return service.save(null, request);
    }

    @PutMapping("/{id}")
    public PurchaseOrderResponse update(
            @PathVariable Long id, @Valid @RequestBody PurchaseOrderRequest request) {
        return service.save(id, request);
    }

    @PatchMapping("/{id}/receive")
    public PurchaseOrderResponse receive(@PathVariable Long id) {
        return service.receive(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        service.cancel(id);
    }
}
