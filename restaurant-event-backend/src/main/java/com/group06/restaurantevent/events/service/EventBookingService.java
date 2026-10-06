package com.group06.restaurantevent.events.service;

import com.group06.restaurantevent.common.enums.EventBookingStatus;
import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.common.exception.ConflictException;
import com.group06.restaurantevent.common.exception.ForbiddenException;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import com.group06.restaurantevent.events.dto.request.CreateEventBookingRequest;
import com.group06.restaurantevent.events.dto.response.EventBookingResponse;
import com.group06.restaurantevent.events.dto.response.EventHallResponse;
import com.group06.restaurantevent.events.dto.response.EventPackageResponse;
import com.group06.restaurantevent.events.entity.EventBooking;
import com.group06.restaurantevent.events.entity.EventHall;
import com.group06.restaurantevent.events.entity.EventPackage;
import com.group06.restaurantevent.events.repository.EventBookingRepository;
import com.group06.restaurantevent.events.repository.EventHallRepository;
import com.group06.restaurantevent.events.repository.EventPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EventBookingService {

    private final EventBookingRepository bookingRepository;
    private final EventHallRepository hallRepository;
    private final EventPackageRepository packageRepository;

    public List<EventHallResponse> listHalls() {
        return hallRepository.findByIsActiveTrueOrderByNameAsc().stream().map(this::toHallResponse).toList();
    }

    public List<EventPackageResponse> listPackages() {
        return packageRepository.findByIsActiveTrueOrderByNameAsc().stream().map(this::toPackageResponse).toList();
    }

    @Transactional
    public EventBookingResponse createBooking(Long customerId, CreateEventBookingRequest req) {
        EventHall hall = hallRepository.findById(req.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Event hall not found"));
        EventPackage pkg = packageRepository.findById(req.getPackageId())
                .orElseThrow(() -> new ResourceNotFoundException("Event package not found"));

        if (req.getGuestCount() < pkg.getMinimumGuests() || req.getGuestCount() > pkg.getMaximumGuests())
            throw new BadRequestException("Guest count must be between " + pkg.getMinimumGuests()
                    + " and " + pkg.getMaximumGuests() + " for this package");

        if (!bookingRepository.findOverlapping(hall.getId(), req.getEventDate(),
                req.getStartTime(), req.getEndTime()).isEmpty())
            throw new ConflictException("This hall is already booked for the selected time slot");

        BigDecimal deposit = pkg.getBasePrice().multiply(BigDecimal.valueOf(0.3));

        EventBooking booking = EventBooking.builder()
                .bookingReference(generateRef())
                .customerId(customerId)
                .hall(hall)
                .eventPackage(pkg)
                .eventDate(req.getEventDate())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .guestCount(req.getGuestCount())
                .specialRequirements(req.getSpecialRequirements())
                .status(EventBookingStatus.PENDING)
                .depositAmount(deposit)
                .build();

        return toResponse(bookingRepository.save(booking));
    }

    public List<EventBookingResponse> myBookings(Long customerId) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toResponse).toList();
    }

    public EventBookingResponse getBooking(Long id, Long customerId) {
        EventBooking b = findBooking(id);
        if (!b.getCustomerId().equals(customerId))
            throw new ForbiddenException("Access denied");
        return toResponse(b);
    }

    @Transactional
    public EventBookingResponse cancelBooking(Long id, Long customerId) {
        EventBooking b = findBooking(id);
        if (!b.getCustomerId().equals(customerId))
            throw new ForbiddenException("Access denied");
        if (b.getStatus() != EventBookingStatus.PENDING && b.getStatus() != EventBookingStatus.CONFIRMED)
            throw new BadRequestException("Booking cannot be cancelled in its current state");
        b.setStatus(EventBookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(b));
    }

    public List<EventBookingResponse> allBookings() {
        return bookingRepository.findAllByOrderByEventDateDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public EventBookingResponse approveBooking(Long id) {
        EventBooking b = findBooking(id);
        if (b.getStatus() != EventBookingStatus.PENDING)
            throw new BadRequestException("Only PENDING bookings can be approved");
        b.setStatus(EventBookingStatus.CONFIRMED);
        return toResponse(bookingRepository.save(b));
    }

    @Transactional
    public EventBookingResponse rejectBooking(Long id, Map<String, String> body) {
        EventBooking b = findBooking(id);
        if (b.getStatus() != EventBookingStatus.PENDING)
            throw new BadRequestException("Only PENDING bookings can be rejected");
        String reason = body.get("rejectionReason");
        if (reason == null || reason.isBlank())
            throw new BadRequestException("Rejection reason is required");
        b.setStatus(EventBookingStatus.REJECTED);
        b.setRejectionReason(reason);
        return toResponse(bookingRepository.save(b));
    }

    private EventBooking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event booking not found: " + id));
    }

    private String generateRef() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = String.format("%04X", new Random().nextInt(0xFFFF));
        return "EVT-" + ts + "-" + suffix;
    }

    private EventBookingResponse toResponse(EventBooking b) {
        return EventBookingResponse.builder()
                .id(b.getId()).bookingReference(b.getBookingReference())
                .customerId(b.getCustomerId())
                .hallId(b.getHall().getId()).hallName(b.getHall().getName())
                .packageId(b.getEventPackage().getId()).packageName(b.getEventPackage().getName())
                .eventDate(b.getEventDate()).startTime(b.getStartTime()).endTime(b.getEndTime())
                .guestCount(b.getGuestCount()).specialRequirements(b.getSpecialRequirements())
                .status(b.getStatus().name()).rejectionReason(b.getRejectionReason())
                .depositAmount(b.getDepositAmount()).createdAt(b.getCreatedAt())
                .build();
    }

    private EventHallResponse toHallResponse(EventHall h) {
        return EventHallResponse.builder().id(h.getId()).name(h.getName())
                .capacity(h.getCapacity()).location(h.getLocation())
                .description(h.getDescription()).isActive(h.isActive()).build();
    }

    private EventPackageResponse toPackageResponse(EventPackage p) {
        return EventPackageResponse.builder().id(p.getId()).name(p.getName())
                .eventType(p.getEventType()).description(p.getDescription())
                .basePrice(p.getBasePrice()).minimumGuests(p.getMinimumGuests())
                .maximumGuests(p.getMaximumGuests()).isActive(p.isActive()).build();
    }
}
