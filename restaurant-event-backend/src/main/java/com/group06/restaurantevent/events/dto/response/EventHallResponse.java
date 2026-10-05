package com.group06.restaurantevent.events.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventHallResponse {
    private Long id;
    private String name;
    private int capacity;
    private String location;
    private String description;
    private boolean isActive;
}
