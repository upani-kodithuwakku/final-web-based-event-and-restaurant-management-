package com.group06.restaurantevent.menu.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MenuItemResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private int preparationMinutes;
    private boolean isAvailable;
    private boolean isActive;

    @com.fasterxml.jackson.annotation.JsonProperty("isAvailable")
    public boolean isAvailable() { return isAvailable; }

    @com.fasterxml.jackson.annotation.JsonProperty("isActive")
    public boolean isActive() { return isActive; }
}
