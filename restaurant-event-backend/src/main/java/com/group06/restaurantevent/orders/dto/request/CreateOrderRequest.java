package com.group06.restaurantevent.orders.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private Long tableId;
    private Long reservationId;
    private String orderType = "DINE_IN";
    @jakarta.validation.constraints.Size(max = 500)
    private String specialNote;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;

        @NotNull(message = "Quantity is required")
        @jakarta.validation.constraints.Min(1)
        @jakarta.validation.constraints.Max(99)
        private Integer quantity;

        @jakarta.validation.constraints.Size(max = 500)
        private String specialNote;
    }
}
