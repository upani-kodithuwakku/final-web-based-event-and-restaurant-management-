package com.group06.restaurantevent.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(max=100)
    private String fullName;

    @NotBlank @Email
    @Size(max=150)
    private String email;

    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
    @Size(max=72)
    @com.group06.restaurantevent.common.validation.PasswordBytes
    private String password;

    @Pattern(regexp="^$|[0-9]{10}", message="Phone number must contain exactly 10 digits")
    private String phone;
}
