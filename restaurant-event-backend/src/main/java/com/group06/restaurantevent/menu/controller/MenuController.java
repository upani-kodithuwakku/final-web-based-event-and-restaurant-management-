package com.group06.restaurantevent.menu.controller;

import com.group06.restaurantevent.menu.dto.response.CategoryResponse;
import com.group06.restaurantevent.menu.dto.response.MenuItemResponse;
import com.group06.restaurantevent.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> listCategories() {
        return ResponseEntity.ok(menuService.listCategories());
    }

    @GetMapping("/items")
    public ResponseEntity<List<MenuItemResponse>> listItems(
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(menuService.listItems(categoryId));
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<MenuItemResponse> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getItem(id));
    }
}
