package com.group06.restaurantevent.events.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EventPackageResponse {
    private Long id;
    private String name;
    private String eventType;
    private String description;
    private BigDecimal basePrice;
    private int minimumGuests;
    private int maximumGuests;
    private boolean isActive;
}
