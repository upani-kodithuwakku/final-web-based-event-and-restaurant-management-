package com.group06.restaurantevent.events.repository;

import com.group06.restaurantevent.common.enums.EventBookingStatus;
import com.group06.restaurantevent.events.entity.EventBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface EventBookingRepository extends JpaRepository<EventBooking, Long> {

    List<EventBooking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<EventBooking> findByStatusOrderByEventDateAsc(EventBookingStatus status);

    List<EventBooking> findAllByOrderByEventDateDesc();

    @Query("SELECT b FROM EventBooking b WHERE b.hall.id = :hallId " +
           "AND b.eventDate = :date " +
           "AND b.status IN ('PENDING','CONFIRMED') " +
           "AND NOT (b.endTime <= :startTime OR b.startTime >= :endTime)")
    List<EventBooking> findOverlapping(@Param("hallId") Long hallId,
                                       @Param("date") LocalDate date,
                                       @Param("startTime") LocalTime startTime,
                                       @Param("endTime") LocalTime endTime);
}
