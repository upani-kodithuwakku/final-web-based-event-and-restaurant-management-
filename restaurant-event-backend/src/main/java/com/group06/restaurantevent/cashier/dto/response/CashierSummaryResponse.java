package com.group06.restaurantevent.cashier.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CashierSummaryResponse(BigDecimal todayRevenue, BigDecimal todayCardRevenue,
        BigDecimal todayOutletRevenue, Balance unpaidInvoices, Balance awaitingCollection,
        List<RecentPayment> recentPayments) {
    public record Balance(long count, BigDecimal total) {}
    public record RecentPayment(String reference, String type, BigDecimal amount, String method, LocalDateTime paidAt) {}
}
