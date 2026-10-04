package com.group06.restaurantevent.staff.repository;

import com.group06.restaurantevent.staff.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByShiftDateOrderByStartTimeAsc(LocalDate date);
    List<Shift> findAllByOrderByShiftDateAscStartTimeAsc();
}
