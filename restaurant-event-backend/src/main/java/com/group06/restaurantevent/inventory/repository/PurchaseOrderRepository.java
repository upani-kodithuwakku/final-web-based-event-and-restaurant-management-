package com.group06.restaurantevent.inventory.repository;

import com.group06.restaurantevent.inventory.entity.PurchaseOrder;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PurchaseOrder p where p.id=:id")
    Optional<PurchaseOrder> findLocked(@Param("id") Long id);
}
