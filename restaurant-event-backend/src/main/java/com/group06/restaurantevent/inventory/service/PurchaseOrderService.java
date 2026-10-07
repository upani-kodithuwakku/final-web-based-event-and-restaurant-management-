package com.group06.restaurantevent.inventory.service;

import com.group06.restaurantevent.common.exception.*;
import com.group06.restaurantevent.inventory.dto.request.PurchaseOrderRequest;
import com.group06.restaurantevent.inventory.dto.response.PurchaseOrderResponse;
import com.group06.restaurantevent.inventory.entity.*;
import com.group06.restaurantevent.inventory.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {
    private final PurchaseOrderRepository orders;
    private final SupplierRepository suppliers;
    private final InventoryItemRepository items;
    private final InventoryService inventory;

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> list() {
        return orders.findAll().stream()
                .sorted(Comparator.comparing(PurchaseOrder::getId).reversed())
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse get(Long id) {
        return response(
                orders.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Purchase order not found")));
    }

    public PurchaseOrderResponse save(Long id, PurchaseOrderRequest request) {
        var supplier =
                suppliers
                        .findById(request.supplierId())
                        .filter(Supplier::isActive)
                        .orElseThrow(() -> new BadRequestException("Choose an active supplier"));
        var order = id == null ? new PurchaseOrder() : find(id);
        if (!order.getStatus().equals("PENDING"))
            throw new ConflictException("Only pending purchase orders can be edited");
        order.setSupplierId(supplier.getId());
        order.setNotes(request.notes());
        if (id == null)
            order.setPoNumber(
                    "PO-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        order.getItems().clear();
        Set<Long> seen = new HashSet<>();
        for (var line : request.items()) {
            if (!seen.add(line.inventoryItemId()))
                throw new BadRequestException("Each stock item must appear only once");
            items.findById(line.inventoryItemId())
                    .filter(InventoryItem::isActive)
                    .orElseThrow(() -> new BadRequestException("Choose active stock items"));
            var row = new PurchaseOrderItem();
            row.setPurchaseOrder(order);
            row.setInventoryItemId(line.inventoryItemId());
            row.setQuantityOrdered(line.quantity());
            row.setUnitCost(line.unitCost());
            order.getItems().add(row);
        }
        return response(orders.save(order));
    }

    public PurchaseOrderResponse receive(Long id) {
        var order =
                orders.findLocked(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Purchase order not found"));
        if (!order.getStatus().equals("PENDING"))
            throw new ConflictException("Only a pending order can be received, and only once");
        // Stable item order reduces deadlocks when two deliveries share inventory items.
        for (var line :
                order.getItems().stream()
                        .sorted(Comparator.comparing(PurchaseOrderItem::getInventoryItemId))
                        .toList()) {
            inventory.receivePurchase(
                    line.getInventoryItemId(), line.getQuantityOrdered(), order.getId());
            line.setQuantityReceived(line.getQuantityOrdered());
        }
        order.setStatus("RECEIVED");
        order.setReceivedAt(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Colombo")));
        return response(orders.save(order));
    }

    public void cancel(Long id) {
        var order = find(id);
        if (!order.getStatus().equals("PENDING"))
            throw new ConflictException("Only pending purchase orders can be cancelled");
        order.setStatus("CANCELLED");
        orders.save(order);
    }

    private PurchaseOrder find(Long id) {
        return orders.findLocked(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
    }

    private PurchaseOrderResponse response(PurchaseOrder p) {
        return new PurchaseOrderResponse(
                p.getId(),
                p.getPoNumber(),
                p.getSupplierId(),
                suppliers.findById(p.getSupplierId()).map(Supplier::getName).orElse("Supplier"),
                p.getStatus(),
                p.getOrderedAt(),
                p.getReceivedAt(),
                p.getNotes(),
                p.getItems().stream()
                        .map(
                                i ->
                                        new PurchaseOrderResponse.Line(
                                                i.getInventoryItemId(),
                                                items.findById(i.getInventoryItemId())
                                                        .map(InventoryItem::getName)
                                                        .orElse("Stock item"),
                                                i.getQuantityOrdered(),
                                                i.getQuantityReceived(),
                                                i.getUnitCost()))
                        .toList());
    }
}
