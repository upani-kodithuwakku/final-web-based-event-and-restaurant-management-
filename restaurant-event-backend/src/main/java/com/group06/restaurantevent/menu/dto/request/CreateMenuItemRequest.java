package com.group06.restaurantevent.menu.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateMenuItemRequest {
    @NotNull(message = "Category ID is required")
    @Positive
    private Long categoryId;

    @NotBlank(message = "Item name is required")
    @Size(max=100)
    private String name;

    @Size(max=1000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Digits(integer=10,fraction=2)
    private BigDecimal price;

    @Size(max=500)
    @jakarta.validation.constraints.Pattern(regexp = "^(https?://[^\\s]+|/(?!/)[^\\s]*)?$", message = "Use an HTTP(S) image URL or a local image path")
    private String imageUrl;
    @Min(1) @Max(240)
    private int preparationMinutes = 15;
    private boolean isAvailable = true;
}
