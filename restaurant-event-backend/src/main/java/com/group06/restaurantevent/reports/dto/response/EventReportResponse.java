package com.group06.restaurantevent.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class EventReportResponse {
    private LocalDate from;
    private LocalDate to;
    private long totalBookings;
    /** Count per status, in EventBookingStatus order (zero counts included). */
    private Map<String, Long> byStatus;
    private long confirmedGuests;
    /** Sum of package prices for confirmed bookings. */
    private BigDecimal confirmedValue;
}
