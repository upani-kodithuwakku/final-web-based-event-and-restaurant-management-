package com.group06.restaurantevent.payment.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerPaymentResponse {
    private Long id;
    private String paymentReference;
    private Long customerId;
    private String purpose;
    private Long foodOrderId;
    private Long eventBookingId;
    private Long tableReservationId;
    private String targetReference;
    private BigDecimal amount;
    private String method;
    private String status;
    private String cardHolderName;
    private String cardLast4;
    private String cardBrand;
    private String gatewayReference;
    private String confirmationMessage;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
