package com.group06.restaurantevent.staff.repository;

import com.group06.restaurantevent.staff.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByShiftDateOrderByStartTimeAsc(LocalDate date);
    List<Shift> findAllByOrderByShiftDateAscStartTimeAsc();
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select s from Shift s where s.id=:id")
    java.util.Optional<Shift> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);
}
