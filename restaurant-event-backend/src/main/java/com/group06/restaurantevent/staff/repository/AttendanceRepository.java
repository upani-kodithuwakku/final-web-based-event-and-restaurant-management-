package com.group06.restaurantevent.staff.repository;

import com.group06.restaurantevent.staff.entity.AttendanceRecord;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    boolean existsByAssignment_Id(Long assignmentId);
}
