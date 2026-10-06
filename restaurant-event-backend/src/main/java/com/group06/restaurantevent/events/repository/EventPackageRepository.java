package com.group06.restaurantevent.events.repository;

import com.group06.restaurantevent.events.entity.EventPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventPackageRepository extends JpaRepository<EventPackage, Long> {
    List<EventPackage> findByIsActiveTrueOrderByNameAsc();
}
