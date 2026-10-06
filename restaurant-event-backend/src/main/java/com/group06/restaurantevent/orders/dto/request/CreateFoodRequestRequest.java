package com.group06.restaurantevent.orders.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateFoodRequestRequest {
    private Long menuItemId;

    @NotBlank(message = "Request message is required")
    @Size(max = 500, message = "Request message must be 500 characters or fewer")
    private String message;
}
