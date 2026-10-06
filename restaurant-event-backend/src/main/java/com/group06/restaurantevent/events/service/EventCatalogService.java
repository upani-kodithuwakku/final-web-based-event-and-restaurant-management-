package com.group06.restaurantevent.events.service;
import com.group06.restaurantevent.events.dto.request.*;
import com.group06.restaurantevent.events.dto.response.*;
import com.group06.restaurantevent.events.entity.*;
import com.group06.restaurantevent.events.repository.*;
import com.group06.restaurantevent.common.exception.*;
import com.group06.restaurantevent.common.enums.EventBookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional
public class EventCatalogService {
 private final EventHallRepository halls;
 private final EventPackageRepository packages;
 private final EventBookingRepository bookings;
 private final EventBookingService bookingService;
 public List<EventHallResponse> halls() { return bookingService.listHalls(); }
 public List<EventPackageResponse> packages() { return bookingService.listPackages(); }
 public EventHallResponse saveHall(Long id, EventHallRequest r) {
  EventHall h = id == null ? new EventHall() : halls.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hall not found"));
  if(halls.findAll().stream().anyMatch(x -> !x.getId().equals(id) && x.getName().equalsIgnoreCase(r.name().trim()))) throw new ConflictException("Hall name already exists");
  if(bookings.findAll().stream().anyMatch(x -> x.getHall().getId().equals(id) && active(x) && x.getGuestCount() > r.capacity())) throw new ConflictException("Capacity cannot be reduced below an active booking's guest count");
  h.setName(r.name().trim()); h.setCapacity(r.capacity()); h.setLocation(r.location().trim()); h.setDescription(r.description()); h.setActive(true); halls.save(h);
  return EventHallResponse.builder().id(h.getId()).name(h.getName()).capacity(h.getCapacity()).location(h.getLocation()).description(h.getDescription()).isActive(h.isActive()).build();
 }
 public EventPackageResponse savePackage(Long id, EventPackageRequest r) {
  if(r.maximumGuests() < r.minimumGuests()) throw new BadRequestException("Maximum guests must be at least minimum guests");
  EventPackage p = id == null ? new EventPackage() : packages.findById(id).orElseThrow(() -> new ResourceNotFoundException("Package not found"));
  if(packages.findAll().stream().anyMatch(x -> !x.getId().equals(id) && x.getName().equalsIgnoreCase(r.name().trim()))) throw new ConflictException("Package name already exists");
  if(id != null && bookings.findAll().stream().anyMatch(x -> x.getEventPackage().getId().equals(id) && active(x))) throw new ConflictException("This package has active bookings; create a new package to change its price or guest limits");
  p.setName(r.name().trim());p.setEventType(r.eventType());p.setDescription(r.description());p.setBasePrice(r.basePrice());p.setMinimumGuests(r.minimumGuests());p.setMaximumGuests(r.maximumGuests());p.setActive(true);packages.save(p);
  return EventPackageResponse.builder().id(p.getId()).name(p.getName()).eventType(p.getEventType()).description(p.getDescription()).basePrice(p.getBasePrice()).minimumGuests(p.getMinimumGuests()).maximumGuests(p.getMaximumGuests()).isActive(p.isActive()).build();
 }
 public void deleteHall(Long id) {
  EventHall h=halls.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hall not found"));
  if(bookings.findAll().stream().anyMatch(x -> x.getHall().getId().equals(id) && active(x))) throw new ConflictException("Hall has active bookings");
  h.setActive(false);halls.save(h);
 }
 public void deletePackage(Long id) {
  EventPackage p=packages.findById(id).orElseThrow(() -> new ResourceNotFoundException("Package not found"));
  if(bookings.findAll().stream().anyMatch(x -> x.getEventPackage().getId().equals(id) && active(x))) throw new ConflictException("Package has active bookings");
  p.setActive(false);packages.save(p);
 }
 private boolean active(EventBooking b) { return b.getStatus()==EventBookingStatus.PENDING || b.getStatus()==EventBookingStatus.CONFIRMED; }
}
