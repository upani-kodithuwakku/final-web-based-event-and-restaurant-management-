package com.group06.restaurantevent.payment.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The customer's bill: food, event and reservation deposit totals, plus the combined total.
 * A total is null when the customer has nothing confirmed in that section.
 */
@Data
@Builder
public class PaymentSummaryResponse {
    private List<BillLine> foodOrders;
    private List<BillLine> eventBookings;
    private List<BillLine> tableReservations;
    private BigDecimal reservationTotal;
    private BigDecimal depositPerGuest;
    private BigDecimal foodTotal;
    private BigDecimal eventTotal;
    private BigDecimal grandTotal;
    private BigDecimal amountPaid;
    private BigDecimal amountDue;

    @Data
    @Builder
    public static class BillLine {
        private String purpose;
        private Long targetId;
        private String reference;
        private String description;
        private LocalDate eventDate;
        private LocalDateTime createdAt;
        private String targetStatus;
        /** True once staff have confirmed (kitchen accepted the order, coordinator confirmed the event). */
        private boolean confirmed;
        /** True when the customer can pay it now; only payable lines are counted in totals. */
        private boolean payable;
        private BigDecimal subtotal;
        private BigDecimal serviceCharge;
        private BigDecimal total;
        private Long paymentId;
        private String paymentReference;
        private String paymentMethod;
        private String paymentStatus;
        private String cardLast4;
        /** Itemised bill lines: dishes for food orders, the package for events. */
        private List<BillItem> items;
    }

    @Data
    @Builder
    public static class BillItem {
        private String name;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }
}
