package com.group06.restaurantevent.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SalesReportResponse {
    private LocalDate from;
    private LocalDate to;
    /** Food orders that were not cancelled. */
    private long orderCount;
    private long cancelledOrders;
    private BigDecimal foodRevenue;
    private BigDecimal averageOrderValue;
    private List<TopItem> topItems;

    @Data
    @Builder
    public static class TopItem {
        private String name;
        private long quantity;
        private BigDecimal revenue;
    }
}
