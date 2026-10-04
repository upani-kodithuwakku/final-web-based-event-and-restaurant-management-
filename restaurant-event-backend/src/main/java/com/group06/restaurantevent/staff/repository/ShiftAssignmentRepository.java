package com.group06.restaurantevent.staff.repository;

import com.group06.restaurantevent.staff.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByShift_Id(Long shiftId);
    Optional<ShiftAssignment> findByShift_IdAndStaffId(Long shiftId, Long staffId);
    List<ShiftAssignment> findByStaffIdOrderByCreatedAtDesc(Long staffId);

    @Query("SELECT a FROM ShiftAssignment a WHERE a.staffId = :staffId " +
           "AND a.shift.shiftDate = :date " +
           "AND NOT (a.shift.endTime <= :startTime OR a.shift.startTime >= :endTime)")
    List<ShiftAssignment> findOverlapping(@Param("staffId") Long staffId,
                                          @Param("date") LocalDate date,
                                          @Param("startTime") LocalTime startTime,
                                          @Param("endTime") LocalTime endTime);
}
