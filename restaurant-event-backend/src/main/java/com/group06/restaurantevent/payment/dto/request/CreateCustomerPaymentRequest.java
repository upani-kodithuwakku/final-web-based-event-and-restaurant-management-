package com.group06.restaurantevent.payment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCustomerPaymentRequest {
    /** Set exactly one of foodOrderId, eventBookingId or tableReservationId. */
    private Long foodOrderId;
    private Long eventBookingId;
    private Long tableReservationId;

    @NotBlank(message = "Payment method is required")
    private String method; // CARD or PAY_AT_OUTLET

    @Valid
    private CardDetailsRequest card;

}
