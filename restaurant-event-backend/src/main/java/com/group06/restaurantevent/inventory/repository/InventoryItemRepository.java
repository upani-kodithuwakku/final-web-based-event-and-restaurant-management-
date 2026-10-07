package com.group06.restaurantevent.inventory.repository;

import com.group06.restaurantevent.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByIsActiveTrueOrderByNameAsc();
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryItem i where i.id = :id")
    java.util.Optional<InventoryItem> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);

    @Query("SELECT i FROM InventoryItem i WHERE i.isActive = true AND i.currentQuantity <= i.reorderLevel")
    List<InventoryItem> findLowStock();
}
