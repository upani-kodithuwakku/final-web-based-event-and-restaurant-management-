package com.group06.restaurantevent.menu.repository;

import com.group06.restaurantevent.menu.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    List<MenuCategory> findByIsActiveTrueOrderByDisplayOrderAsc();
    boolean existsByNameIgnoreCase(String name);
}
