package com.group06.restaurantevent.orders.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long menuItemId;
    private String itemNameSnapshot;
    private BigDecimal unitPriceSnapshot;
    private int quantity;
    private String specialNote;
    private BigDecimal lineTotal;
}
