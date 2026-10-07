package com.group06.restaurantevent.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminResetPasswordRequest {
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Size(max = 72, message = "Password must be at most 72 characters")
    @com.group06.restaurantevent.common.validation.PasswordBytes
    private String password;
}
