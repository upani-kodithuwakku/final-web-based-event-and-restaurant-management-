package com.group06.restaurantevent.staff.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateStaffProfileRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    private String jobTitle;
    private String employmentStatus = "FULL_TIME";
    private LocalDate joinedDate;
}
