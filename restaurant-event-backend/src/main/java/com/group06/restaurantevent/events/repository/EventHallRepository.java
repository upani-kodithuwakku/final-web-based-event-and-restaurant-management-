package com.group06.restaurantevent.events.repository;

import com.group06.restaurantevent.events.entity.EventHall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventHallRepository extends JpaRepository<EventHall, Long> {
    List<EventHall> findByIsActiveTrueOrderByNameAsc();
}
