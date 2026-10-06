package com.group06.restaurantevent.users.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    @NotNull(message = "active is required")
    private Boolean active;
}
