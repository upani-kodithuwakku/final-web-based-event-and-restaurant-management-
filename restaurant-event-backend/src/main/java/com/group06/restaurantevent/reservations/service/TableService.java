package com.group06.restaurantevent.reservations.service;

import com.group06.restaurantevent.common.enums.TableStatus;
import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.common.exception.ConflictException;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import com.group06.restaurantevent.reservations.dto.request.CreateTableRequest;
import com.group06.restaurantevent.reservations.dto.request.UpdateTableStatusRequest;
import com.group06.restaurantevent.reservations.dto.response.TableResponse;
import com.group06.restaurantevent.reservations.entity.RestaurantTable;
import com.group06.restaurantevent.reservations.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TableService {

    private final RestaurantTableRepository tableRepository;
    private final com.group06.restaurantevent.reservations.repository.TableReservationRepository reservations;

    public List<TableResponse> getAllTables() {
        return tableRepository.findAll().stream().map(this::toResponse).toList();
    }

    public TableResponse getTable(Long id) {
        return toResponse(findActiveById(id));
    }

    public List<TableResponse> getActiveTables() {
        return tableRepository.findAllByIsActiveTrue().stream().map(this::toResponse).toList();
    }

    @Transactional
    public TableResponse createTable(CreateTableRequest request) {
        if (tableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new ConflictException("Table number already exists: " + request.getTableNumber());
        }
        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(request.getTableNumber())
                .capacity(request.getCapacity())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .location(request.getLocation())
                .currentStatus(TableStatus.AVAILABLE)
                .isActive(true)
                .build();
        return toResponse(tableRepository.save(table));
    }

    @Transactional
    public TableResponse updateTable(Long id, CreateTableRequest request) {
        RestaurantTable table = findActiveById(id);
        if (!table.getTableNumber().equals(request.getTableNumber())
                && tableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new ConflictException("Table number already in use: " + request.getTableNumber());
        }
        if(reservations.findAll().stream().anyMatch(r -> r.getTable().getId().equals(id) && !r.getReservationDate().isBefore(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Colombo"))) && java.util.List.of(com.group06.restaurantevent.common.enums.ReservationStatus.PENDING, com.group06.restaurantevent.common.enums.ReservationStatus.CONFIRMED, com.group06.restaurantevent.common.enums.ReservationStatus.CHECKED_IN).contains(r.getStatus()) && r.getGuestCount()>request.getCapacity())) throw new ConflictException("Capacity cannot be reduced below an upcoming reservation's guest count");
        table.setTableNumber(request.getTableNumber().trim());
        table.setCapacity(request.getCapacity());
        if(request.getDisplayName()!=null) table.setDisplayName(request.getDisplayName());
        if(request.getDescription()!=null) table.setDescription(request.getDescription());
        if(request.getImageUrl()!=null) table.setImageUrl(request.getImageUrl());
        table.setLocation(request.getLocation());
        return toResponse(tableRepository.save(table));
    }

    @Transactional
    public TableResponse updateTableStatus(Long id, UpdateTableStatusRequest request) {
        RestaurantTable table = findActiveById(id);
        var upcoming = reservations.findAll().stream().filter(r -> r.getTable().getId().equals(id)
                && !r.getReservationDate().isBefore(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Colombo")))
                && java.util.List.of(com.group06.restaurantevent.common.enums.ReservationStatus.PENDING,
                    com.group06.restaurantevent.common.enums.ReservationStatus.CONFIRMED,
                    com.group06.restaurantevent.common.enums.ReservationStatus.CHECKED_IN).contains(r.getStatus())).toList();
        if (request.getStatus() == TableStatus.OUT_OF_SERVICE && !upcoming.isEmpty())
            throw new ConflictException("Cancel or reassign upcoming reservations before taking this table out of service");
        if (upcoming.stream().anyMatch(r -> r.getStatus() == com.group06.restaurantevent.common.enums.ReservationStatus.CHECKED_IN)
                && request.getStatus() != TableStatus.OCCUPIED)
            throw new ConflictException("Complete the checked-in reservation before changing this table's status");
        table.setCurrentStatus(request.getStatus());
        return toResponse(tableRepository.save(table));
    }

    @Transactional
    public void deleteTable(Long id) {
        RestaurantTable table = findActiveById(id);
        if (table.getCurrentStatus() == TableStatus.OCCUPIED || table.getCurrentStatus() == TableStatus.RESERVED) {
            throw new BadRequestException("Cannot deactivate a table that is currently reserved or occupied");
        }
        if(reservations.findAll().stream().anyMatch(r -> r.getTable().getId().equals(id) && !r.getReservationDate().isBefore(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Colombo"))) && java.util.List.of(com.group06.restaurantevent.common.enums.ReservationStatus.PENDING, com.group06.restaurantevent.common.enums.ReservationStatus.CONFIRMED, com.group06.restaurantevent.common.enums.ReservationStatus.CHECKED_IN).contains(r.getStatus()))) throw new ConflictException("Table has active or upcoming reservations");
        table.setActive(false);
        tableRepository.save(table);
    }

    public RestaurantTable findActiveById(Long id) {
        return tableRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found: " + id));
    }

    public TableResponse toResponse(RestaurantTable t) {
        return TableResponse.builder()
                .id(t.getId())
                .tableNumber(t.getTableNumber())
                .capacity(t.getCapacity())
                .displayName(t.getDisplayName())
                .description(t.getDescription())
                .imageUrl(t.getImageUrl())
                .location(t.getLocation())
                .currentStatus(t.getCurrentStatus())
                .isActive(t.isActive())
                .build();
    }
}
