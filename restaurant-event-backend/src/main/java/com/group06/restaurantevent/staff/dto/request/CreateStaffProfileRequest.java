package com.group06.restaurantevent.staff.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateStaffProfileRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank @Size(max=100)
    private String jobTitle;
    private String employmentStatus = "FULL_TIME";
    @PastOrPresent
    private LocalDate joinedDate;
}
