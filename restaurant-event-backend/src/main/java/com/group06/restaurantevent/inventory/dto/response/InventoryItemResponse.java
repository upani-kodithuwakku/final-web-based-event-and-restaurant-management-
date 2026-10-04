package com.group06.restaurantevent.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InventoryItemResponse {
    private Long id;
    private String name;
    private String unit;
    private BigDecimal currentQuantity;
    private BigDecimal reorderLevel;
    private boolean lowStock;
    private boolean isActive;

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }
}
