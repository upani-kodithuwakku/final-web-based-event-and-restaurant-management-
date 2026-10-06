package com.group06.restaurantevent.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePaymentStatusRequest {
    @NotBlank(message = "Status is required")
    private String status; // PENDING, PAID, FAILED, REFUNDED
}
