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
        InventoryItem item = InventoryItem.builder()
                .name(req.getName()).unit(req.getUnit())
                .currentQuantity(req.getCurrentQuantity())
                .reorderLevel(req.getReorderLevel())
                .isActive(true).build();
        return toResponse(itemRepository.save(item));
    }

    @Transactional
    public InventoryItemResponse updateItem(Long id, CreateInventoryItemRequest req) {
        InventoryItem item = findItem(id);
        item.setName(req.getName());
        item.setUnit(req.getUnit());
        item.setReorderLevel(req.getReorderLevel());
        BigDecimal delta = req.getCurrentQuantity().subtract(item.getCurrentQuantity());
        if (delta.signum() != 0) {
            adjustStock(id, delta, "Quantity updated from inventory editor");
        }
        return toResponse(itemRepository.save(item));
    }

    @Transactional
    public InventoryItemResponse adjustStock(Long id, BigDecimal delta, String note) {
        if (delta == null || delta.signum() == 0) throw new BadRequestException("Stock adjustment must be non-zero");
        InventoryItem item = findItem(id);
        BigDecimal newQty = item.getCurrentQuantity().add(delta);
        if (newQty.compareTo(BigDecimal.ZERO) < 0)
            throw new ConflictException("Stock cannot go below zero");

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

        return toResponse(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        InventoryItem item = findItem(id);
        item.setActive(false);
        itemRepository.save(item);
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
