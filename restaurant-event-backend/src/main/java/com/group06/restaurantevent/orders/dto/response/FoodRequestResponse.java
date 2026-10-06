package com.group06.restaurantevent.orders.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FoodRequestResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long menuItemId;
    private String itemName;
    private String message;
    private String status;
    private Long resolvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
