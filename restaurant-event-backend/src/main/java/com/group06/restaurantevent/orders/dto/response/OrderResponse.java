package com.group06.restaurantevent.orders.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderReference;
    private Long customerId;
    private Long tableId;
    private Long reservationId;
    private String orderType;
    private String status;
    private String specialNote;
    private BigDecimal subtotal;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
