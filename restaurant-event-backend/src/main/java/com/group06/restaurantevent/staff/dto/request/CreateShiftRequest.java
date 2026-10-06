package com.group06.restaurantevent.staff.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateShiftRequest {
    @NotNull(message = "Shift date is required")
    @FutureOrPresent
    private LocalDate shiftDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank @Size(max=50)
    private String roleRequired;
    @Min(1) @Max(100)
    private int requiredStaffCount = 1;
}
