package com.group06.restaurantevent.inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateInventoryItemRequest {
    @NotBlank(message = "Item name is required")
    @Size(max=100)
    private String name;

    @NotBlank @Size(max=20)
    private String unit;

    @NotNull
    @DecimalMin("0")
    @Digits(integer=9,fraction=3)
    private BigDecimal currentQuantity = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0")
    @Digits(integer=9,fraction=3)
    private BigDecimal reorderLevel = BigDecimal.ZERO;
}
