package com.group06.restaurantevent.staff.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class CreateStaffUserRequest {

    @NotBlank(message = "Full name is required")
    @Size(max=100)
    private String fullName;

    @NotBlank @Email(message = "Valid email is required")
    @Size(max=150)
    private String email;

    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
    @Size(max=72)
    private String password;

    @Pattern(regexp="^$|[0-9]{10}", message="Phone number must contain exactly 10 digits")
    private String phone;

    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;

    @Size(max=100)
    private String jobTitle;
    private String employmentStatus = "FULL_TIME";
    @PastOrPresent
    private LocalDate joinedDate;
}
