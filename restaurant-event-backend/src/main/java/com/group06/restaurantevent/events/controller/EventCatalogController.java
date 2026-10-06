package com.group06.restaurantevent.events.controller;
import com.group06.restaurantevent.events.service.EventCatalogService;
import com.group06.restaurantevent.events.dto.request.*;
import com.group06.restaurantevent.events.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import java.util.List;
@RestController @RequestMapping("/api/event-coordinator/catalog") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('EVENT_COORDINATOR','ADMIN','MANAGER')")
public class EventCatalogController {
 private final EventCatalogService service;
 @GetMapping("/halls") public List<EventHallResponse> halls() { return service.halls(); }
 @PostMapping("/halls") @ResponseStatus(HttpStatus.CREATED) public EventHallResponse addHall(@Valid @RequestBody EventHallRequest r) { return service.saveHall(null,r); }
 @PutMapping("/halls/{id}") public EventHallResponse editHall(@PathVariable Long id,@Valid @RequestBody EventHallRequest r) { return service.saveHall(id,r); }
 @DeleteMapping("/halls/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteHall(@PathVariable Long id) { service.deleteHall(id); }
 @GetMapping("/packages") public List<EventPackageResponse> packages() { return service.packages(); }
 @PostMapping("/packages") @ResponseStatus(HttpStatus.CREATED) public EventPackageResponse addPackage(@Valid @RequestBody EventPackageRequest r) { return service.savePackage(null,r); }
 @PutMapping("/packages/{id}") public EventPackageResponse editPackage(@PathVariable Long id,@Valid @RequestBody EventPackageRequest r) { return service.savePackage(id,r); }
 @DeleteMapping("/packages/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deletePackage(@PathVariable Long id) { service.deletePackage(id); }
}
