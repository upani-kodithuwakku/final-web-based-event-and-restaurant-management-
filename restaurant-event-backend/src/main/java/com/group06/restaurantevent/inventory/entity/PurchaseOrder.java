package com.group06.restaurantevent.inventory.entity;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "po_number", nullable = false, unique = true, length = 30)
    private String poNumber;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @PrePersist
    void created() {
        orderedAt = LocalDateTime.now(java.time.ZoneId.of("Asia/Colombo"));
    }
}
