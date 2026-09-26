package com.group06.restaurantevent.reservations.dto.request;

import com.group06.restaurantevent.common.enums.TableStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTableStatusRequest {
    @NotNull
    private TableStatus status;
}
