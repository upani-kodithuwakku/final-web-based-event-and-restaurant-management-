package com.group06.restaurantevent.events.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record EventHallRequest(@NotBlank @Size(max=100) String name, @Min(1) @Max(1000) int capacity, @NotBlank @Size(max=100) String location, @Size(max=1000) String description) {}
