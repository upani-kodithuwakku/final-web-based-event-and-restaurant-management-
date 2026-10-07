package com.group06.restaurantevent.menu.service;

import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import com.group06.restaurantevent.menu.dto.request.CreateCategoryRequest;
import com.group06.restaurantevent.menu.dto.request.CreateMenuItemRequest;
import com.group06.restaurantevent.menu.dto.response.CategoryResponse;
import com.group06.restaurantevent.menu.dto.response.MenuItemResponse;
import com.group06.restaurantevent.menu.entity.MenuCategory;
import com.group06.restaurantevent.menu.entity.MenuItem;
import com.group06.restaurantevent.menu.repository.MenuCategoryRepository;
import com.group06.restaurantevent.menu.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;

    public List<CategoryResponse> listCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream().map(this::toCategoryResponse).toList();
    }

    public List<MenuItemResponse> listItems(Long categoryId) {
        List<MenuItem> items = categoryId != null
                ? itemRepository.findByCategoryIdAndIsActiveTrueOrderByNameAsc(categoryId)
                : itemRepository.findByIsActiveTrueOrderByNameAsc();
        return items.stream().map(this::toItemResponse).toList();
    }

    public List<MenuItemResponse> listAllItems() {
        return itemRepository.findByIsActiveTrueOrderByNameAsc()
                .stream().map(this::toItemResponse).toList();
    }

    public MenuItemResponse getItem(Long id) {
        return toItemResponse(findItem(id));
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest req) {
        if (categoryRepository.existsByNameIgnoreCase(req.getName()))
            throw new BadRequestException("Category name already exists");
        MenuCategory cat = MenuCategory.builder()
                .name(req.getName().trim())
                .description(req.getDescription())
                .displayOrder(req.getDisplayOrder())
                .isActive(true)
                .build();
        return toCategoryResponse(categoryRepository.save(cat));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CreateCategoryRequest req) {
        MenuCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if(categoryRepository.findAll().stream().anyMatch(x -> !x.getId().equals(id) && x.getName().equalsIgnoreCase(req.getName().trim()))) throw new BadRequestException("Category name already exists");
        cat.setName(req.getName().trim());
        cat.setDescription(req.getDescription());
        cat.setDisplayOrder(req.getDisplayOrder());
        return toCategoryResponse(categoryRepository.save(cat));
    }

    @Transactional
    public void deleteCategory(Long id) {
        MenuCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (itemRepository.findAll().stream().anyMatch(i -> i.isActive() && i.getCategory().getId().equals(id)))
            throw new com.group06.restaurantevent.common.exception.ConflictException("Move or remove this category's dishes before removing the category");
        cat.setActive(false);
        categoryRepository.save(cat);
    }

    @Transactional
    public MenuItemResponse createItem(CreateMenuItemRequest req) {
        MenuCategory cat = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if(!cat.isActive()) throw new BadRequestException("Choose an active category");
        MenuItem item = MenuItem.builder()
                .category(cat)
                .name(req.getName().trim())
                .description(req.getDescription())
                .price(req.getPrice())
                .imageUrl(req.getImageUrl())
                .preparationMinutes(req.getPreparationMinutes())
                .isAvailable(req.isAvailable())
                .isActive(true)
                .build();
        return toItemResponse(itemRepository.save(item));
    }

    @Transactional
    public MenuItemResponse updateItem(Long id, CreateMenuItemRequest req) {
        MenuItem item = findItem(id);
        MenuCategory cat = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if(!cat.isActive()) throw new BadRequestException("Choose an active category");
        item.setCategory(cat);
        item.setName(req.getName());
        item.setDescription(req.getDescription());
        item.setPrice(req.getPrice());
        item.setImageUrl(req.getImageUrl());
        item.setPreparationMinutes(req.getPreparationMinutes());
        item.setAvailable(req.isAvailable());
        return toItemResponse(itemRepository.save(item));
    }

    @Transactional
    public MenuItemResponse toggleAvailability(Long id, boolean available) {
        MenuItem item = findItem(id);
        item.setAvailable(available);
        return toItemResponse(itemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long id) {
        MenuItem item = findItem(id);
        item.setActive(false);
        itemRepository.save(item);
    }

    public MenuItem findItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
    }

    private CategoryResponse toCategoryResponse(MenuCategory c) {
        return CategoryResponse.builder()
                .id(c.getId()).name(c.getName())
                .description(c.getDescription())
                .displayOrder(c.getDisplayOrder())
                .isActive(c.isActive())
                .build();
    }

    public MenuItemResponse toItemResponse(MenuItem i) {
        return MenuItemResponse.builder()
                .id(i.getId())
                .categoryId(i.getCategory().getId())
                .categoryName(i.getCategory().getName())
                .name(i.getName()).description(i.getDescription())
                .price(i.getPrice()).imageUrl(i.getImageUrl())
                .preparationMinutes(i.getPreparationMinutes())
                .isAvailable(i.isAvailable()).isActive(i.isActive())
                .build();
    }
}
