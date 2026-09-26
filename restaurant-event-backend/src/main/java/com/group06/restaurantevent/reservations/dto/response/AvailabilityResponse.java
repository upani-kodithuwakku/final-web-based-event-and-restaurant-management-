package com.group06.restaurantevent.reservations.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {
    private List<TableResponse> availableTables;
    private List<LocalTime> alternativeTimes;
    private String message;
}
