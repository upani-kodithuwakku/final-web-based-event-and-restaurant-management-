package com.group06.restaurantevent.orders.entity;

import com.group06.restaurantevent.common.enums.FoodRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_requests", indexes = @Index(name = "idx_food_request_status", columnList = "status,created_at"))
@Getter @Setter @NoArgsConstructor
public class FoodRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;
    @Column(name = "menu_item_id")
    private Long menuItemId;
    @Column(name = "item_name", length = 150)
    private String itemName;
    @Column(nullable = false, length = 500)
    private String message;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FoodRequestStatus status = FoodRequestStatus.OPEN;
    @Column(name = "resolved_by")
    private Long resolvedBy;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    @PrePersist void created() { createdAt = LocalDateTime.now(); }
}
