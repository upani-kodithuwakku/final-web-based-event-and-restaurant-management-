package com.group06.restaurantevent.menu.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private int displayOrder;
    private boolean isActive;

    @com.fasterxml.jackson.annotation.JsonProperty("isActive")
    public boolean isActive() { return isActive; }
}
