package com.group06.restaurantevent.payment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Lets a customer change how they pay before the payment is completed. */
@Data
public class UpdateCustomerPaymentRequest {
    @NotBlank(message = "Payment method is required")
    private String method; // CARD or PAY_AT_OUTLET

    @Valid
    private CardDetailsRequest card;

}
