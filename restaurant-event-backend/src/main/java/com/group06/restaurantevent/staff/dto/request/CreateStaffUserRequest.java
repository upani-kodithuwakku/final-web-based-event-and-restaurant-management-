package com.group06.restaurantevent.staff.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class CreateStaffUserRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank @Email(message = "Valid email is required")
    private String email;

    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String phone;

    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;

    private String jobTitle;
    private String employmentStatus = "FULL_TIME";
    private LocalDate joinedDate;
}
