package com.group06.restaurantevent.inventory.repository;

import com.group06.restaurantevent.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByIsActiveTrueOrderByNameAsc();

    @Query("SELECT i FROM InventoryItem i WHERE i.isActive = true AND i.currentQuantity <= i.reorderLevel")
    List<InventoryItem> findLowStock();
}
