package com.group06.restaurantevent.reports.dto.response;

import lombok.Builder;
import lombok.Data;

/** Today's snapshot. The first five fields keep the original dashboard JSON keys. */
@Data
@Builder
public class DashboardReportResponse {
    private String date;
    private long todayReservations;
    private long totalTables;
    private long availableTables;
    private long occupiedTables;
    private long reservedTables;
    private long outOfServiceTables;
    private long totalCustomers;
    private long activeFoodOrders;
    private long pendingEventBookings;
    private long lowStockItems;
}
