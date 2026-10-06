package com.group06.restaurantevent.inventory.controller;
import com.group06.restaurantevent.inventory.dto.request.SupplierRequest;
import com.group06.restaurantevent.inventory.entity.Supplier;
import com.group06.restaurantevent.inventory.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class SupplierController {
    private final SupplierService service;
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','WAITER','KITCHEN_STAFF','INVENTORY_MANAGER','CASHIER','EVENT_COORDINATOR')")
    public List<Supplier> list() { return service.list(); }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','WAITER','KITCHEN_STAFF','INVENTORY_MANAGER','CASHIER','EVENT_COORDINATOR')")
    public Supplier get(@PathVariable Long id) { return service.get(id); }
    @PostMapping
    public ResponseEntity<Supplier> create(@Valid @RequestBody SupplierRequest req) { return ResponseEntity.status(HttpStatus.CREATED).body(service.save(null, req)); }
    @PutMapping("/{id}")
    public Supplier update(@PathVariable Long id, @Valid @RequestBody SupplierRequest req) { return service.save(id, req); }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
