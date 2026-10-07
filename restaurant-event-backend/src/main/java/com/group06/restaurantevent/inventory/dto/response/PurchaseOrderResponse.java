package com.group06.restaurantevent.inventory.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseOrderResponse(
        Long id,
        String poNumber,
        Long supplierId,
        String supplierName,
        String status,
        LocalDateTime orderedAt,
        LocalDateTime receivedAt,
        String notes,
        List<Line> items) {
    public record Line(
            Long inventoryItemId,
            String itemName,
            BigDecimal quantityOrdered,
            BigDecimal quantityReceived,
            BigDecimal unitCost) {}
}
