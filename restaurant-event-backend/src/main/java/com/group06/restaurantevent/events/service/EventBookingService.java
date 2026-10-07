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
    private final com.group06.restaurantevent.users.repository.UserRepository userRepository;

    private final com.group06.restaurantevent.payment.repository.CustomerPaymentRepository payments;
    private final com.group06.restaurantevent.billing.repository.InvoiceRepository invoices;

    public List<EventHallResponse> listHalls() {
        return hallRepository.findByIsActiveTrueOrderByNameAsc().stream().map(this::toHallResponse).toList();
    }

    public List<EventPackageResponse> listPackages() {
        return packageRepository.findByIsActiveTrueOrderByNameAsc().stream().map(this::toPackageResponse).toList();
    }

    @Transactional
    public EventBookingResponse createBooking(String email, CreateEventBookingRequest req) {
        Long customerId = customerId(email);
        EventHall hall = hallRepository.findById(req.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Event hall not found"));
        EventPackage pkg = packageRepository.findById(req.getPackageId())
                .orElseThrow(() -> new ResourceNotFoundException("Event package not found"));

        if (!hall.isActive() || !pkg.isActive())
            throw new BadRequestException("This hall or package is no longer available");
        if (req.getGuestCount() > hall.getCapacity())
            throw new BadRequestException("The selected hall is too small for your guest count");
        if (!req.getEndTime().isAfter(req.getStartTime()))
            throw new BadRequestException("End time must be after start time");
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

    @Transactional(readOnly = true)
    public List<EventBookingResponse> myBookings(String email) {
        Long customerId = customerId(email);
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EventBookingResponse getBooking(Long id, String email) {
        Long customerId = customerId(email);
        EventBooking b = findBooking(id);
        if (!b.getCustomerId().equals(customerId))
            throw new ForbiddenException("Access denied");
        return toResponse(b);
    }

    @Transactional
    public EventBookingResponse cancelBooking(Long id, String email) {
        Long customerId = customerId(email);
        EventBooking b = findBooking(id);
        if (!b.getCustomerId().equals(customerId))
            throw new ForbiddenException("Access denied");
        if (b.getStatus() != EventBookingStatus.PENDING && b.getStatus() != EventBookingStatus.CONFIRMED)
            throw new BadRequestException("Booking cannot be cancelled in its current state");
        b.setStatus(EventBookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(b));
    }

    @Transactional(readOnly = true)
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
        String reason = body.get("reason");
        if (reason == null) reason = body.get("rejectionReason");
        if (reason == null || reason.isBlank() || reason.length() > 500)
            throw new BadRequestException("Rejection reason is required");
        b.setStatus(EventBookingStatus.REJECTED);
        b.setRejectionReason(reason);
        return toResponse(bookingRepository.save(b));
    }

    @Transactional
    public EventBookingResponse updateBooking(Long id, String email, CreateEventBookingRequest request) {
        EventBooking booking = findBooking(id);
        if (!booking.getCustomerId().equals(customerId(email))) throw new ForbiddenException("Access denied");
        if (booking.getStatus() != EventBookingStatus.PENDING && booking.getStatus() != EventBookingStatus.CONFIRMED)
            throw new ConflictException("Only pending or confirmed enquiries can be edited");
        if (!booking.getEventDate().atTime(booking.getStartTime()).isAfter(LocalDateTime.now(java.time.ZoneId.of("Asia/Colombo"))))
            throw new ConflictException("Past event bookings cannot be edited");
        if (payments.existsByEventBookingId(id) || invoices.findByEventBookingId(id).isPresent())
            throw new ConflictException("Contact staff to change an event with a payment or invoice");
        EventHall hall = hallRepository.findById(request.getHallId()).orElseThrow(() -> new ResourceNotFoundException("Event hall not found"));
        EventPackage pkg = packageRepository.findById(request.getPackageId()).orElseThrow(() -> new ResourceNotFoundException("Event package not found"));
        if (!hall.isActive() || !pkg.isActive()) throw new BadRequestException("Choose an active hall and package");
        if (request.getGuestCount() > hall.getCapacity() || request.getGuestCount() < pkg.getMinimumGuests() || request.getGuestCount() > pkg.getMaximumGuests())
            throw new BadRequestException("Guest count must fit the hall and package");
        if (!request.getEndTime().isAfter(request.getStartTime())) throw new BadRequestException("End time must be after start time");
        if (bookingRepository.findOverlapping(hall.getId(), request.getEventDate(), request.getStartTime(), request.getEndTime()).stream().anyMatch(b -> !b.getId().equals(id)))
            throw new ConflictException("This hall is already booked for the selected time slot");
        booking.setHall(hall); booking.setEventPackage(pkg); booking.setEventDate(request.getEventDate());
        booking.setStartTime(request.getStartTime()); booking.setEndTime(request.getEndTime());
        booking.setGuestCount(request.getGuestCount()); booking.setSpecialRequirements(request.getSpecialRequirements());
        booking.setDepositAmount(pkg.getBasePrice().multiply(new BigDecimal("0.30")).setScale(2, java.math.RoundingMode.HALF_UP));
        booking.setStatus(EventBookingStatus.PENDING);
        return toResponse(bookingRepository.save(booking));
    }

    private Long customerId(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found")).getId();
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
