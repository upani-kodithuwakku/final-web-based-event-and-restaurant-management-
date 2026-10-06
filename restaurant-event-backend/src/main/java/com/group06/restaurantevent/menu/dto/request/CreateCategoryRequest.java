package com.group06.restaurantevent.menu.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    @NotBlank(message = "Category name is required")
    @Size(max=100)
    private String name;
    @Size(max=500)
    private String description;
    @Min(0)
    private int displayOrder = 0;
}
