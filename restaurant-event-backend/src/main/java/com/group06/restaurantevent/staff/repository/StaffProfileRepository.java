package com.group06.restaurantevent.staff.repository;

import com.group06.restaurantevent.staff.entity.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {
    List<StaffProfile> findByIsActiveTrueOrderByEmployeeCodeAsc();
    Optional<StaffProfile> findByUserId(Long userId);
    boolean existsByEmployeeCode(String code);
}
