package com.group06.restaurantevent.reservations.dto.response;

import com.group06.restaurantevent.common.enums.TableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableResponse {
    private Long id;
    private String tableNumber;
    private int capacity;

    private String displayName;
    private String description;
    private String imageUrl;
    private String location;
    private TableStatus currentStatus;
    private boolean isActive;

    // Lombok would expose this as "active"; the frontend expects "isActive".
    @com.fasterxml.jackson.annotation.JsonProperty("isActive")
    public boolean isActive() { return isActive; }
}
