package com.group06.restaurantevent.inventory.service;

import com.group06.restaurantevent.common.enums.MovementType;
import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.common.exception.ConflictException;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import com.group06.restaurantevent.inventory.dto.request.CreateInventoryItemRequest;
import com.group06.restaurantevent.inventory.dto.response.InventoryItemResponse;
import com.group06.restaurantevent.inventory.entity.InventoryItem;
import com.group06.restaurantevent.inventory.entity.StockMovement;
import com.group06.restaurantevent.inventory.repository.InventoryItemRepository;
import com.group06.restaurantevent.inventory.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository itemRepository;
    private final StockMovementRepository movementRepository;
    private final com.group06.restaurantevent.inventory.repository.PurchaseOrderRepository purchaseOrders;
    private final com.group06.restaurantevent.users.repository.UserRepository users;
    private final com.group06.restaurantevent.notifications.service.NotificationFactory notifications;

    public List<InventoryItemResponse> listAll() {
        return itemRepository.findByIsActiveTrueOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    public List<InventoryItemResponse> listLowStock() {
        return itemRepository.findLowStock().stream().map(this::toResponse).toList();
    }

    public InventoryItemResponse getItem(Long id) {
        return toResponse(findItem(id));
    }

    @Transactional
    public InventoryItemResponse createItem(CreateInventoryItemRequest req) {
        requireUniqueName(null, req.getName());
        InventoryItem item = InventoryItem.builder()
                .name(req.getName().trim()).unit(req.getUnit().trim())
                .currentQuantity(req.getCurrentQuantity())
                .reorderLevel(req.getReorderLevel())
                .isActive(true).build();
        itemRepository.save(item);
        if (item.getCurrentQuantity().signum() > 0) movementRepository.save(StockMovement.builder()
                .inventoryItemId(item.getId()).movementType(MovementType.ADJUSTMENT)
                .quantityChange(item.getCurrentQuantity()).referenceType("INITIAL")
                .note("Initial stock").build());
        notifyLowStock(item);
        return toResponse(item);
    }

    @Transactional
    public InventoryItemResponse updateItem(Long id, CreateInventoryItemRequest req) {
        InventoryItem item = lockedItem(id);
        requireUniqueName(id, req.getName());
        boolean wasLow = item.getCurrentQuantity().compareTo(item.getReorderLevel()) <= 0;
        item.setName(req.getName().trim());
        item.setUnit(req.getUnit().trim());
        item.setReorderLevel(req.getReorderLevel());
        BigDecimal delta = req.getCurrentQuantity().subtract(item.getCurrentQuantity());
        if (delta.signum() != 0) {
            adjustStock(id, delta, "Quantity updated from inventory editor");
        }
        if (delta.signum() == 0 && !wasLow && item.getCurrentQuantity().compareTo(item.getReorderLevel()) <= 0) notifyLowStock(item);
        return toResponse(itemRepository.save(item));
    }

    @Transactional
    public InventoryItemResponse adjustStock(Long id, BigDecimal delta, String note) {
        if (delta == null || delta.signum() == 0) throw new BadRequestException("Stock adjustment must be non-zero");
        InventoryItem item = lockedItem(id);
        boolean wasLow = item.getCurrentQuantity().compareTo(item.getReorderLevel()) <= 0;
        BigDecimal newQty = item.getCurrentQuantity().add(delta);
        if (newQty.compareTo(BigDecimal.ZERO) < 0)
            throw new ConflictException("Stock cannot go below zero");

        if (newQty.compareTo(new BigDecimal("999999999.999")) > 0) throw new BadRequestException("Stock exceeds the quantity limit");
        item.setCurrentQuantity(newQty);
        itemRepository.save(item);

        StockMovement movement = StockMovement.builder()
                .inventoryItemId(item.getId())
                .movementType(delta.compareTo(BigDecimal.ZERO) >= 0 ? MovementType.ADJUSTMENT : MovementType.CONSUMPTION)
                .quantityChange(delta)
                .note(note)
                .referenceType("MANUAL")
                .build();
        movementRepository.save(movement);
        if (!wasLow && newQty.compareTo(item.getReorderLevel()) <= 0) notifyLowStock(item);
        return toResponse(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        InventoryItem item = findItem(id);
        if (purchaseOrders.findAll().stream().anyMatch(p -> p.getStatus().equals("PENDING") && p.getItems().stream().anyMatch(i -> i.getInventoryItemId().equals(id))))
            throw new ConflictException("Receive, edit or cancel pending purchase orders before removing this stock item");
        item.setActive(false);
        itemRepository.save(item);
    }

    @Transactional
    public void receivePurchase(Long itemId, BigDecimal quantity, Long orderId) {
        InventoryItem item = lockedItem(itemId);
        BigDecimal next = item.getCurrentQuantity().add(quantity);
        if (next.compareTo(new BigDecimal("999999999.999")) > 0) throw new BadRequestException("Received stock exceeds the quantity limit");
        item.setCurrentQuantity(next); itemRepository.save(item);
        movementRepository.save(StockMovement.builder().inventoryItemId(itemId).movementType(MovementType.PURCHASE)
            .quantityChange(quantity).referenceType("PURCHASE_ORDER").referenceId(orderId).note("Purchase order received").build());
    }

    public List<StockMovement> history(Long id) {
        if (!itemRepository.existsById(id)) throw new ResourceNotFoundException("Inventory item not found");
        return movementRepository.findByInventoryItemIdOrderByCreatedAtDesc(id);
    }

    private InventoryItem lockedItem(Long id) {
        return itemRepository.findLockedById(id).filter(InventoryItem::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
    }

    private void requireUniqueName(Long id, String name) {
        if (itemRepository.findAll().stream().anyMatch(i -> i.isActive() && !i.getId().equals(id) && i.getName().trim().equalsIgnoreCase(name.trim())))
            throw new ConflictException("An active inventory item already uses this name");
    }

    private void notifyLowStock(InventoryItem item) {
        if (item.getCurrentQuantity().compareTo(item.getReorderLevel()) <= 0)
            users.findAll().stream().filter(u -> u.isActive() && u.getRoles().stream().anyMatch(r -> java.util.Set.of("ADMIN", "MANAGER", "INVENTORY_MANAGER").contains(r.getName())))
                .forEach(u -> notifications.lowStock(u, item.getName()));
    }

    public InventoryItem findItem(Long id) {
        return itemRepository.findById(id).filter(InventoryItem::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found: " + id));
    }

    public InventoryItemResponse toResponse(InventoryItem i) {
        boolean lowStock = i.getCurrentQuantity().compareTo(i.getReorderLevel()) <= 0;
        return InventoryItemResponse.builder()
                .id(i.getId()).name(i.getName()).unit(i.getUnit())
                .currentQuantity(i.getCurrentQuantity())
                .reorderLevel(i.getReorderLevel())
                .lowStock(lowStock).isActive(i.isActive()).build();
    }
}
