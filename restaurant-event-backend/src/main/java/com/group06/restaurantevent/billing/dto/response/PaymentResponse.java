package com.group06.restaurantevent.billing.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private String paymentReference;
    private Long invoiceId;
    private BigDecimal amount;
    private String method;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
