package com.group06.restaurantevent.reservations.service;

import com.group06.restaurantevent.common.audit.AuditLog;
import com.group06.restaurantevent.common.audit.AuditLogRepository;
import com.group06.restaurantevent.common.enums.ReservationStatus;
import com.group06.restaurantevent.common.enums.TableStatus;
import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.common.exception.ConflictException;
import com.group06.restaurantevent.common.exception.ForbiddenException;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import com.group06.restaurantevent.reservations.observer.ReservationEvent;
import com.group06.restaurantevent.reservations.observer.ReservationSubject;
import com.group06.restaurantevent.reservations.dto.request.AdminCreateReservationRequest;
import com.group06.restaurantevent.reservations.dto.request.AdminUpdateReservationRequest;
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
    private final ReservationSubject reservationSubject;
    private final com.group06.restaurantevent.payment.service.CustomerPaymentService paymentService;
    private final AuditLogRepository auditLogRepository;

    @Value("${app.reservation.default-duration-minutes:120}")
    private int defaultDurationMinutes;

    public AvailabilityResponse checkAvailability(LocalDate date, LocalTime time, int guests, String preference) {
        validateSlot(date, time, guests);
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
            if (alt.isBefore(LocalTime.of(11, 0)) || alt.isAfter(LocalTime.of(21, 0)) || !date.atTime(alt).isAfter(LocalDateTime.now(java.time.ZoneId.of("Asia/Colombo")))) continue;
            LocalTime altEnd = alt.plusMinutes(defaultDurationMinutes);
            boolean hasSlot = candidates.stream()
                    .filter(t -> t.getCurrentStatus() != TableStatus.OUT_OF_SERVICE)
                    .filter(t -> preference == null || preference.isBlank() || preference.equalsIgnoreCase(t.getLocation()))
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
        return createFor(findUserByEmail(email), request);
    }

    @Transactional
    public ReservationResponse createReservationForCustomer(String staffEmail, AdminCreateReservationRequest request) {
        ReservationResponse created = createFor(findUserByEmail(request.getCustomerEmail()), request);
        audit(staffEmail, "RESERVATION_CREATED_BY_STAFF", created.getId(), null, summary(created));
        return created;
    }

    private ReservationResponse createFor(User customer, CreateReservationRequest request) {
        validateSlot(request.getReservationDate(), request.getStartTime(), request.getGuestCount());
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
        publishChange(reservation, null, "CREATED");
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
        return toResponse(applyUpdate(reservation, request, null));
    }

    @Transactional
    public ReservationResponse cancelReservation(Long id, String email, CancelReservationRequest request) {
        User customer = findUserByEmail(email);
        TableReservation reservation = findById(id);

        if (!reservation.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("You do not have access to this reservation");
        }
        return toResponse(applyCancel(reservation, request.getReason(), email));
    }

    /** Shared by the customer and staff edit flows so both enforce the same rules. */
    private TableReservation applyUpdate(TableReservation reservation, UpdateReservationRequest request, Long newTableId) {
        if (reservation.getStatus() != ReservationStatus.CONFIRMED && reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BadRequestException("Only PENDING or CONFIRMED reservations can be modified");
        }
        if (reservation.getReservationDate().isBefore(LocalDate.now(java.time.ZoneId.of("Asia/Colombo")))) {
            throw new BadRequestException("Cannot modify a past reservation");
        }

        RestaurantTable table = reservation.getTable();
        if (newTableId != null && !newTableId.equals(table.getId())) {
            table = tableService.findActiveById(newTableId);
            if (table.getCurrentStatus() == TableStatus.OUT_OF_SERVICE) {
                throw new BadRequestException("Table is out of service and cannot be reserved");
            }
        }

        LocalDate newDate = request.getReservationDate() != null ? request.getReservationDate() : reservation.getReservationDate();
        LocalTime newStart = request.getStartTime() != null ? request.getStartTime() : reservation.getStartTime();
        LocalTime newEnd = newStart.plusMinutes(defaultDurationMinutes);
        int newGuests = request.getGuestCount() != null ? request.getGuestCount() : reservation.getGuestCount();

        validateSlot(newDate, newStart, newGuests);
        if (table.getCurrentStatus() == TableStatus.OUT_OF_SERVICE) {
            throw new BadRequestException("Table is out of service");
        }
        if (table.getCapacity() < newGuests) {
            throw new BadRequestException("Table capacity insufficient for updated guest count");
        }

        List<TableReservation> overlapping = reservationRepository.findOverlappingExcluding(
                table.getId(), newDate, newStart, newEnd, reservation.getId());
        if (!overlapping.isEmpty()) {
            throw new ConflictException("Updated time slot conflicts with an existing reservation");
        }

        reservation.setTable(table);
        reservation.setReservationDate(newDate);
        reservation.setStartTime(newStart);
        reservation.setEndTime(newEnd);
        reservation.setGuestCount(newGuests);
        if (request.getSeatingPreference() != null) reservation.setSeatingPreference(request.getSeatingPreference());
        if (request.getSpecialRequest() != null) reservation.setSpecialRequest(request.getSpecialRequest());
        if (request.getContactName() != null) reservation.setContactName(request.getContactName());
        if (request.getContactPhone() != null) reservation.setContactPhone(request.getContactPhone());

        reservationRepository.save(reservation);
        publishChange(reservation, reservation.getStatus(), "UPDATED");
        return reservation;
    }

    private TableReservation applyCancel(TableReservation reservation, String reason, String actorEmail) {
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Reservation is already cancelled");
        }
        if (reservation.getStatus() == ReservationStatus.COMPLETED || reservation.getStatus() == ReservationStatus.CHECKED_IN
                || reservation.getStatus() == ReservationStatus.NO_SHOW) {
            throw new BadRequestException("Cannot cancel a completed, checked-in or no-show reservation");
        }
        ReservationStatus previous = reservation.getStatus();
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelReason(reason);
        reservationRepository.save(reservation);
        paymentService.cancelReservationPayment(reservation.getId(), actorEmail);
        publishChange(reservation, previous, "CANCELLED");
        return reservation;
    }

    // ---- Staff endpoints ----

    public List<ReservationResponse> getReservationsByDate(LocalDate date, ReservationStatus status) {
        List<ReservationStatus> statuses = status != null
                ? List.of(status)
                : List.of(ReservationStatus.values());
        return reservationRepository.findByReservationDateAndStatusIn(date, statuses)
                .stream().map(this::toResponse).toList();
    }

    public ReservationResponse getReservation(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public ReservationResponse updateReservationAsStaff(Long id, String staffEmail, AdminUpdateReservationRequest request) {
        TableReservation reservation = findById(id);
        String before = summary(toResponse(reservation));
        ReservationResponse updated = toResponse(applyUpdate(reservation, request, request.getTableId()));
        audit(staffEmail, "RESERVATION_UPDATED_BY_STAFF", id, before, summary(updated));
        return updated;
    }

    @Transactional
    public ReservationResponse cancelReservationAsStaff(Long id, String staffEmail, CancelReservationRequest request) {
        TableReservation reservation = findById(id);
        String before = summary(toResponse(reservation));
        ReservationResponse cancelled = toResponse(applyCancel(reservation, request.getReason(), staffEmail));
        audit(staffEmail, "RESERVATION_CANCELLED_BY_STAFF", id, before, summary(cancelled));
        return cancelled;
    }

    @Transactional
    public ReservationResponse checkIn(Long id) {
        TableReservation reservation = findById(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BadRequestException("Only CONFIRMED reservations can be checked in");
        }
        requireReservationStarted(reservation);
        reservation.setStatus(ReservationStatus.CHECKED_IN);
        RestaurantTable table = reservation.getTable();
        table.setCurrentStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);
        publishChange(reservation, ReservationStatus.CONFIRMED, "CHECKED_IN");
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
        publishChange(reservation, ReservationStatus.CHECKED_IN, "COMPLETED");
        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse markNoShow(Long id) {
        TableReservation reservation = findById(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BadRequestException("Only CONFIRMED reservations can be marked no-show");
        }
        requireReservationStarted(reservation);
        reservation.setStatus(ReservationStatus.NO_SHOW);
        RestaurantTable table = reservation.getTable();
        if (table.getCurrentStatus() == TableStatus.RESERVED) {
            table.setCurrentStatus(TableStatus.AVAILABLE);
            tableRepository.save(table);
        }
        publishChange(reservation, ReservationStatus.CONFIRMED, "NO_SHOW");
        return toResponse(reservationRepository.save(reservation));
    }



    @Transactional
    public ReservationResponse confirm(Long id) {
        TableReservation r = findById(id);
        if (r.getStatus() != ReservationStatus.PENDING) throw new BadRequestException("Only pending reservations can be confirmed");
        r.setStatus(ReservationStatus.CONFIRMED);
        publishChange(r, ReservationStatus.PENDING, "CONFIRMED");
        return toResponse(reservationRepository.save(r));
    }

    private void publishChange(TableReservation reservation, ReservationStatus previous, String action) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        Long actorId = authentication == null ? null : userRepository.findByEmail(authentication.getName())
                .map(User::getId).orElse(null);
        reservationSubject.notifyObservers(new ReservationEvent(reservation.getId(),
                reservation.getBookingReference(), reservation.getCustomer(), actorId, action,
                previous, reservation.getStatus()));
    }

    private void requireReservationStarted(TableReservation reservation) {
        if (reservation.getReservationDate().atTime(reservation.getStartTime()).isAfter(
                LocalDateTime.now(java.time.ZoneId.of("Asia/Colombo"))))
            throw new BadRequestException("Wait until the reservation start time before checking in or marking no-show");
    }

    private void validateSlot(LocalDate date, LocalTime time, int guests) {
        if (date == null || time == null) throw new BadRequestException("A valid reservation date and time are required");
        if (guests < 1 || guests > 200) throw new BadRequestException("Guest count must be between 1 and 200");
        if (time.isBefore(LocalTime.of(11, 0)) || time.isAfter(LocalTime.of(21, 0)))
            throw new BadRequestException("Reservation start time must be between 11:00 and 21:00");
        if (!date.atTime(time).isAfter(LocalDateTime.now(java.time.ZoneId.of("Asia/Colombo"))))
            throw new BadRequestException("Reservation date and time must be in the future");
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

    private void audit(String actorEmail, String action, Long reservationId, String oldValue, String newValue) {
        Long actorId = userRepository.findByEmailAndIsActiveTrue(actorEmail).map(User::getId).orElse(null);
        auditLogRepository.save(AuditLog.builder()
                .userId(actorId)
                .action(action)
                .entityName("TableReservation")
                .entityId(reservationId)
                .oldValue(oldValue)
                .newValue(newValue)
                .build());
    }

    private String summary(ReservationResponse r) {
        return "table=" + r.getTable().getTableNumber() + ", date=" + r.getReservationDate()
                + ", time=" + r.getStartTime() + ", guests=" + r.getGuestCount() + ", status=" + r.getStatus();
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
