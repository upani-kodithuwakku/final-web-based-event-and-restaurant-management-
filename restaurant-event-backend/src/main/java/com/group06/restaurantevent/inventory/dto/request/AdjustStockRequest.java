package com.group06.restaurantevent.inventory.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record AdjustStockRequest(@NotNull @Digits(integer=9,fraction=3) BigDecimal delta, @Size(max=500) String note) {}
