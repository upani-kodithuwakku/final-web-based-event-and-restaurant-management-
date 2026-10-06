package com.group06.restaurantevent.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class InventoryReportResponse {
    private long activeItems;
    private long lowStockCount;
    private List<LowStockItem> lowStockItems;

    @Data
    @Builder
    public static class LowStockItem {
        private Long id;
        private String name;
        private String unit;
        private BigDecimal currentQuantity;
        private BigDecimal reorderLevel;
    }
}
