package com.group06.restaurantevent.events.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class EventBookingResponse {
    private Long id;
    private String bookingReference;
    private Long customerId;
    private Long hallId;
    private String hallName;
    private Long packageId;
    private String packageName;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int guestCount;
    private String specialRequirements;
    private String status;
    private String rejectionReason;
    private BigDecimal depositAmount;
    private LocalDateTime createdAt;
}
