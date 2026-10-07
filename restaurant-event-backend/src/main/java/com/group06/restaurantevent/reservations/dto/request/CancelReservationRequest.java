package com.group06.restaurantevent.reservations.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Size;

@Data
public class CancelReservationRequest {
    @Size(max = 500, message = "Cancellation reason must be at most 500 characters")
    private String reason;
}
