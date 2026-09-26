package com.group06.restaurantevent.reservations.repository;

import com.group06.restaurantevent.common.enums.ReservationStatus;
import com.group06.restaurantevent.reservations.entity.TableReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TableReservationRepository extends JpaRepository<TableReservation, Long> {

    List<TableReservation> findByCustomerIdOrderByReservationDateDescCreatedAtDesc(Long customerId);

    List<TableReservation> findByReservationDateAndStatusIn(LocalDate date, List<ReservationStatus> statuses);

    @Query("""
        SELECT r FROM TableReservation r
        WHERE r.table.id = :tableId
          AND r.reservationDate = :date
          AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN')
          AND r.startTime < :endTime
          AND r.endTime > :startTime
    """)
    List<TableReservation> findOverlapping(
            @Param("tableId") Long tableId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    @Query("""
        SELECT r FROM TableReservation r
        WHERE r.table.id = :tableId
          AND r.reservationDate = :date
          AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN')
          AND r.startTime < :endTime
          AND r.endTime > :startTime
          AND r.id <> :excludeId
    """)
    List<TableReservation> findOverlappingExcluding(
            @Param("tableId") Long tableId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);
}
