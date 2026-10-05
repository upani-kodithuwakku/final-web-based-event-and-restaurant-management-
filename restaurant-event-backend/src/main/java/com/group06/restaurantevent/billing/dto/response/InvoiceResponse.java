package com.group06.restaurantevent.billing.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private Long customerId;
    private Long foodOrderId;
    private Long eventBookingId;
    private String invoiceType;
    private BigDecimal subtotal;
    private BigDecimal serviceCharge;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime createdAt;
    private List<InvoiceItemResponse> items;

    @Data
    @Builder
    public static class InvoiceItemResponse {
        private Long id;
        private String description;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }
}
