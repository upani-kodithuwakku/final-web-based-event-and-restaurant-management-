package com.group06.restaurantevent.reservations.repository;

import com.group06.restaurantevent.common.enums.TableStatus;
import com.group06.restaurantevent.reservations.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    boolean existsByTableNumber(String tableNumber);
    Optional<RestaurantTable> findByIdAndIsActiveTrue(Long id);
    List<RestaurantTable> findAllByIsActiveTrue();
    List<RestaurantTable> findByCurrentStatusAndIsActiveTrue(TableStatus status);
    List<RestaurantTable> findByCapacityGreaterThanEqualAndIsActiveTrue(int capacity);
}
