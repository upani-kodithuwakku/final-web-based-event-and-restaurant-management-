package com.group06.restaurantevent.menu.controller;

import com.group06.restaurantevent.menu.dto.request.CreateCategoryRequest;
import com.group06.restaurantevent.menu.dto.request.CreateMenuItemRequest;
import com.group06.restaurantevent.menu.dto.response.CategoryResponse;
import com.group06.restaurantevent.menu.dto.response.MenuItemResponse;
import com.group06.restaurantevent.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/menu")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class AdminMenuController {

    private final MenuService menuService;

    @GetMapping("/items")
    public ResponseEntity<List<MenuItemResponse>> listAll() {
        return ResponseEntity.ok(menuService.listAllItems());
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createCategory(req));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id,
                                                           @Valid @RequestBody CreateCategoryRequest req) {
        return ResponseEntity.ok(menuService.updateCategory(id, req));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        menuService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/items")
    public ResponseEntity<MenuItemResponse> createItem(@Valid @RequestBody CreateMenuItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createItem(req));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<MenuItemResponse> updateItem(@PathVariable Long id,
                                                       @Valid @RequestBody CreateMenuItemRequest req) {
        return ResponseEntity.ok(menuService.updateItem(id, req));
    }

    @PatchMapping("/items/{id}/availability")
    public ResponseEntity<MenuItemResponse> toggleAvailability(@PathVariable Long id,
                                                               @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(menuService.toggleAvailability(id, body.getOrDefault("available", true)));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        menuService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
