package com.group06.restaurantevent.staff.controller;

import com.group06.restaurantevent.staff.dto.request.AttendanceRequest;
import com.group06.restaurantevent.staff.service.AttendanceService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/staff/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class AttendanceController {
    private final AttendanceService service;

    @GetMapping
    public List<AttendanceService.Response> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceService.Response create(@Valid @RequestBody AttendanceRequest r) {
        return service.save(null, r);
    }

    @PutMapping("/{id}")
    public AttendanceService.Response update(
            @PathVariable Long id, @Valid @RequestBody AttendanceRequest r) {
        return service.save(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
