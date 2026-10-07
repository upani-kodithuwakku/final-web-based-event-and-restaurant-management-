package com.group06.restaurantevent.inventory.entity;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(name = "inventory_item_id", nullable = false)
    private Long inventoryItemId;

    @Column(name = "quantity_ordered", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityOrdered;

    @Column(name = "quantity_received", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityReceived = BigDecimal.ZERO;

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost;
}
