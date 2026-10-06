package com.group06.restaurantevent.menu.repository;

import com.group06.restaurantevent.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryIdAndIsActiveTrueOrderByNameAsc(Long categoryId);
    List<MenuItem> findByIsActiveTrueOrderByNameAsc();
    List<MenuItem> findByIsAvailableTrueAndIsActiveTrueOrderByNameAsc();
}
