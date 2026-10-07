package com.group06.restaurantevent.inventory.controller;

import com.group06.restaurantevent.inventory.dto.request.CreateInventoryItemRequest;
import com.group06.restaurantevent.inventory.dto.response.InventoryItemResponse;
import com.group06.restaurantevent.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('INVENTORY_MANAGER','ADMIN','MANAGER')")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/items")
    public ResponseEntity<List<InventoryItemResponse>> listAll() {
        return ResponseEntity.ok(inventoryService.listAll());
    }

    @GetMapping("/items/low-stock")
    public ResponseEntity<List<InventoryItemResponse>> lowStock() {
        return ResponseEntity.ok(inventoryService.listLowStock());
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<InventoryItemResponse> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getItem(id));
    }

    @PostMapping("/items")
    public ResponseEntity<InventoryItemResponse> createItem(@Valid @RequestBody CreateInventoryItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createItem(req));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<InventoryItemResponse> updateItem(@PathVariable Long id,
                                                            @Valid @RequestBody CreateInventoryItemRequest req) {
        return ResponseEntity.ok(inventoryService.updateItem(id, req));
    }

    @PatchMapping("/items/{id}/adjust")
    public ResponseEntity<InventoryItemResponse> adjustStock(@PathVariable Long id,
                                                             @Valid @RequestBody com.group06.restaurantevent.inventory.dto.request.AdjustStockRequest body) {
        BigDecimal delta = body.delta();
        String note = body.note() == null ? "Manual adjustment" : body.note();
        return ResponseEntity.ok(inventoryService.adjustStock(id, delta, note));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/items/{id}/movements")
    public List<com.group06.restaurantevent.inventory.entity.StockMovement> movements(@PathVariable Long id) {
        return inventoryService.history(id);
    }
}
