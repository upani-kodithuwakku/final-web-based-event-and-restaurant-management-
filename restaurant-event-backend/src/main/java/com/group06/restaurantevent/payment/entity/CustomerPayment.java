package com.group06.restaurantevent.payment.entity;

import com.group06.restaurantevent.common.enums.PaymentOption;
import com.group06.restaurantevent.common.enums.PaymentPurpose;
import com.group06.restaurantevent.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A customer's payment for one food order, event booking or table reservation.
 * Card details are never stored beyond the last four digits.
 */
@Entity
@Table(name = "customer_payments", indexes = {
        @Index(name = "idx_cpay_customer", columnList = "customer_id,created_at"),
        @Index(name = "idx_cpay_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_reference", nullable = false, unique = true, length = 30)
    private String paymentReference;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentPurpose purpose;

    // One payment per order, event booking or reservation; MySQL unique keys allow many NULLs.
    @Column(name = "food_order_id", unique = true)
    private Long foodOrderId;

    @Column(name = "event_booking_id", unique = true)
    private Long eventBookingId;
    @Column(name = "table_reservation_id", unique = true)
    private Long tableReservationId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentOption method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "card_holder_name", length = 100)
    private String cardHolderName;

    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    @Column(name = "card_brand", length = 20)
    private String cardBrand;

    @Column(name = "gateway_reference", length = 100)
    private String gatewayReference;


    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }
}
