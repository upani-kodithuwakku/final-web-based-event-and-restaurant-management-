package com.group06.restaurantevent.billing.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateInvoiceRequest {
    @NotNull(message = "Invoice type is required")
    private String invoiceType; // FOOD_ORDER or EVENT_BOOKING

    private Long foodOrderId;
    private Long eventBookingId;
    private Long customerId;
}
