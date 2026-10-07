package com.group06.restaurantevent.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record PurchaseOrderRequest(
        @NotNull @Positive Long supplierId,
        @Size(max = 500) String notes,
        @NotEmpty @Size(max = 100) @Valid List<Line> items) {
    public record Line(
            @NotNull @Positive Long inventoryItemId,
            @NotNull @DecimalMin("0.001") @Digits(integer = 9, fraction = 3) BigDecimal quantity,
            @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal unitCost) {}
}
