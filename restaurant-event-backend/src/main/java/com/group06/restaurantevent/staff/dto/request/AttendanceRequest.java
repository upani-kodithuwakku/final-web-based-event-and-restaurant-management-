package com.group06.restaurantevent.staff.dto.request;

import com.group06.restaurantevent.common.enums.AttendanceStatus;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record AttendanceRequest(
        @NotNull @Positive Long assignmentId,
        @NotNull AttendanceStatus status,
        @PastOrPresent LocalDateTime checkInAt,
        @PastOrPresent LocalDateTime checkOutAt) {}
