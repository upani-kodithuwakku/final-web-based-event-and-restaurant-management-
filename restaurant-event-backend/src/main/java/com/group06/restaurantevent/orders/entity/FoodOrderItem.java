package com.group06.restaurantevent.orders.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "food_order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private FoodOrder order;

    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;

    @Column(name = "item_name_snapshot", nullable = false, length = 150)
    private String itemNameSnapshot;

    @Column(name = "unit_price_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceSnapshot;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "special_note", length = 300)
    private String specialNote;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;
}
