package com.group06.restaurantevent.staff.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateShiftRequest {
    @NotNull(message = "Shift date is required")
    private LocalDate shiftDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private String roleRequired;
    private int requiredStaffCount = 1;
}
