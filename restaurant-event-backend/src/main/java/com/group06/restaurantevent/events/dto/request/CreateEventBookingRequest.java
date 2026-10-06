package com.group06.restaurantevent.events.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateEventBookingRequest {
    @NotNull(message = "Hall is required")
    private Long hallId;

    @NotNull(message = "Package is required")
    private Long packageId;

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    private LocalDate eventDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @Min(value = 1, message = "Guest count must be at least 1")
    private int guestCount;

    private String specialRequirements;
}
