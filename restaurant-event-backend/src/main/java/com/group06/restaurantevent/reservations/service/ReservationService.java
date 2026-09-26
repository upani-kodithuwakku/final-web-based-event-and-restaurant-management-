package com.group06.restaurantevent.reservations.service;

import com.group06.restaurantevent.common.enums.ReservationStatus;
import com.group06.restaurantevent.common.enums.TableStatus;
import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.common.exception.ConflictException;
import com.group06.restaurantevent.common.exception.ForbiddenException;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import com.group06.restaurantevent.notifications.service.NotificationFactory;
import com.group06.restaurantevent.reservations.dto.request.CancelReservationRequest;
import com.group06.restaurantevent.reservations.dto.request.CreateReservationRequest;
import com.group06.restaurantevent.reservations.dto.request.UpdateReservationRequest;
import com.group06.restaurantevent.reservations.dto.response.AvailabilityResponse;
import com.group06.restaurantevent.reservations.dto.response.ReservationResponse;
import com.group06.restaurantevent.reservations.entity.RestaurantTable;
import com.group06.restaurantevent.reservations.entity.TableReservation;
import com.group06.restaurantevent.reservations.repository.RestaurantTableRepository;
import com.group06.restaurantevent.reservations.repository.TableReservationRepository;
import com.group06.restaurantevent.users.entity.User;
import com.group06.restaurantevent.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final TableReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;
    private final UserRepository userRepository;
    private final TableService tableService;
    private final NotificationFactory notificationFactory;

    @Value("${app.reservation.default-duration-minutes:120}")
    private int defaultDurationMinutes;

    public AvailabilityResponse checkAvailability(LocalDate date, LocalTime time, int guests, String preference) {
        LocalTime endTime = time.plusMinutes(defaultDurationMinutes);
        List<RestaurantTable> candidates = tableRepository.findByCapacityGreaterThanEqualAndIsActiveTrue(guests);

        List<RestaurantTable> available = candidates.stream()
                .filter(t -> t.getCurrentStatus() != TableStatus.OUT_OF_SERVICE)
                .filter(t -> preference == null || preference.isBlank() || preference.equalsIgnoreCase(t.getLocation()))
                .filter(t -> reservationRepository.findOverlapping(t.getId(), date, time, endTime).isEmpty())
                .toList();

        if (!available.isEmpty()) {
            return AvailabilityResponse.builder()
                    .availableTables(available.stream().map(tableService::toResponse).toList())
                    .message("Tables available for your requested time")
                    .build();
        }

        // Build alternative times: check ±30, ±60, ±90 minute slots
        List<LocalTime> alternatives = new ArrayList<>();
        int[] offsets = {-30, 30, -60, 60, -90, 90};
        for (int offset : offsets) {
            LocalTime alt = time.plusMinutes(offset);
            if (alt.isBefore(LocalTime.of(9, 0)) || alt.isAfter(LocalTime.of(22, 0))) continue;
            LocalTime altEnd = alt.plusMinutes(defaultDurationMinutes);
            boolean hasSlot = candidates.stream()
                    .filter(t -> t.getCurrentStatus() != TableStatus.OUT_OF_SERVICE)
                    .anyMatch(t -> reservationRepository.findOverlapping(t.getId(), date, alt, altEnd).isEmpty());
            if (hasSlot) alternatives.add(alt);
        }

        return AvailabilityResponse.builder()
                .availableTables(List.of())
                .alternativeTimes(alternatives.isEmpty() ? null : alternatives)
                .message(alternatives.isEmpty()
                        ? "No tables available for this date"
                        : "No tables at requested time. Alternative slots available.")
                .build();
    }

    @Transactional
    public ReservationResponse createReservation(String email, CreateReservationRequest request) {
        User customer = findUserByEmail(email);
        RestaurantTable table = tableService.findActiveById(request.getTableId());

        if (table.getCurrentStatus() == TableStatus.OUT_OF_SERVICE) {
            throw new BadRequestException("Table is out of service and cannot be reserved");
        }
        if (table.getCapacity() < request.getGuestCount()) {
            throw new BadRequestException("Table capacity (" + table.getCapacity() + ") is less than guest count");
        }

        LocalTime endTime = request.getStartTime().plusMinutes(defaultDurationMinutes);
        List<TableReservation> overlapping = reservationRepository.findOverlapping(
                table.getId(), request.getReservationDate(), request.getStartTime(), endTime);
        if (!overlapping.isEmpty()) {
            throw new ConflictException("Table is already reserved for the requested time slot");
        }

        TableReservation reservation = TableReservation.builder()
                .bookingReference(generateReference(request.getReservationDate()))
                .customer(customer)
                .table(table)
                .reservationDate(request.getReservationDate())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .guestCount(request.getGuestCount())
                .seatingPreference(request.getSeatingPreference())
                .specialRequest(request.getSpecialRequest())
                .status(ReservationStatus.CONFIRMED)
                .contactName(request.getContactName())
                .contactPhone(request.getContactPhone())
                .build();

        reservation = reservationRepository.save(reservation);
        notificationFactory.reservationConfirmed(customer, reservation.getBookingReference());
        return toResponse(reservation);
    }

    public List<ReservationResponse> getMyReservations(String email) {
        User customer = findUserByEmail(email);
        return reservationRepository
                .findByCustomerIdOrderByReservationDateDescCreatedAtDesc(customer.getId())
                .stream().map(this::toResponse).toList();
    }

    public ReservationResponse getReservationForCustomer(Long id, String email) {
        User customer = findUserByEmail(email);
        TableReservation reservation = findById(id);
        if (!reservation.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("You do not have access to this reservation");
        }
        return toResponse(reservation);
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, String email, UpdateReservationRequest request) {
        User customer = findUserByEmail(email);
        TableReservation reservation = findById(id);

        if (!reservation.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("You do not have access to this reservation");
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED && reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BadRequestException("Only PENDING or CONFIRMED reservations can be modified");
        }
        if (reservation.getReservationDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot modify a past reservation");
        }

        LocalDate newDate = request.getReservationDate() != null ? request.getReservationDate() : reservation.getReservationDate();
        LocalTime newStart = request.getStartTime() != null ? request.getStartTime() : reservation.getStartTime();
        LocalTime newEnd = newStart.plusMinutes(defaultDurationMinutes);
        int newGuests = request.getGuestCount() != null ? request.getGuestCount() : reservation.getGuestCount();

        if (reservation.getTable().getCapacity() < newGuests) {
            throw new BadRequestException("Table capacity insufficient for updated guest count");
        }

        List<TableReservation> overlapping = reservationRepository.findOverlappingExcluding(
                reservation.getTable().getId(), newDate, newStart, newEnd, id);
        if (!overlapping.isEmpty()) {
            throw new ConflictException("Updated time slot conflicts with an existing reservation");
        }

        reservation.setReservationDate(newDate);
        reservation.setStartTime(newStart);
        reservation.setEndTime(newEnd);
        reservation.setGuestCount(newGuests);
        if (request.getSeatingPreference() != null) reservation.setSeatingPreference(request.getSeatingPreference());
        if (request.getSpecialRequest() != null) reservation.setSpecialRequest(request.getSpecialRequest());
        if (request.getContactName() != null) reservation.setContactName(request.getContactName());
        if (request.getContactPhone() != null) reservation.setContactPhone(request.getContactPhone());

        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse cancelReservation(Long id, String email, CancelReservationRequest request) {
        User customer = findUserByEmail(email);
        TableReservation reservation = findById(id);

        if (!reservation.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("You do not have access to this reservation");
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Reservation is already cancelled");
        }
        if (reservation.getStatus() == ReservationStatus.COMPLETED || reservation.getStatus() == ReservationStatus.CHECKED_IN) {
            throw new BadRequestException("Cannot cancel a completed or checked-in reservation");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelReason(request.getReason());
        reservationRepository.save(reservation);
        notificationFactory.reservationCancelled(customer, reservation.getBookingReference());
        return toResponse(reservation);
    }

    // ---- Staff endpoints ----

    public List<ReservationResponse> getReservationsByDate(LocalDate date, ReservationStatus status) {
        List<ReservationStatus> statuses = status != null
                ? List.of(status)
                : List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN);
        return reservationRepository.findByReservationDateAndStatusIn(date, statuses)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ReservationResponse checkIn(Long id) {
        TableReservation reservation = findById(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BadRequestException("Only CONFIRMED reservations can be checked in");
        }
        reservation.setStatus(ReservationStatus.CHECKED_IN);
        RestaurantTable table = reservation.getTable();
        table.setCurrentStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);
        notificationFactory.reservationCheckedIn(reservation.getCustomer(), reservation.getBookingReference());
        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse complete(Long id) {
        TableReservation reservation = findById(id);
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new BadRequestException("Only CHECKED_IN reservations can be completed");
        }
        reservation.setStatus(ReservationStatus.COMPLETED);
        RestaurantTable table = reservation.getTable();
        table.setCurrentStatus(TableStatus.AVAILABLE);
        tableRepository.save(table);
        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse markNoShow(Long id) {
        TableReservation reservation = findById(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BadRequestException("Only CONFIRMED reservations can be marked no-show");
        }
        reservation.setStatus(ReservationStatus.NO_SHOW);
        RestaurantTable table = reservation.getTable();
        if (table.getCurrentStatus() == TableStatus.RESERVED) {
            table.setCurrentStatus(TableStatus.AVAILABLE);
            tableRepository.save(table);
        }
        return toResponse(reservationRepository.save(reservation));
    }

    // ---- Helpers ----

    private TableReservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private String generateReference(LocalDate date) {
        String datePart = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04X", new Random().nextInt(0xFFFF));
        return "RES-" + datePart + "-" + random;
    }

    public ReservationResponse toResponse(TableReservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .bookingReference(r.getBookingReference())
                .customerId(r.getCustomer().getId())
                .customerName(r.getCustomer().getFullName())
                .table(tableService.toResponse(r.getTable()))
                .reservationDate(r.getReservationDate())
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .guestCount(r.getGuestCount())
                .seatingPreference(r.getSeatingPreference())
                .specialRequest(r.getSpecialRequest())
                .status(r.getStatus())
                .contactName(r.getContactName())
                .contactPhone(r.getContactPhone())
                .cancelReason(r.getCancelReason())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
