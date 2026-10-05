package com.group06.restaurantevent.billing.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentRequest {
    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;

    @NotNull(message = "Payment method is required")
    private String method; // CASH, CARD, ONLINE
}
