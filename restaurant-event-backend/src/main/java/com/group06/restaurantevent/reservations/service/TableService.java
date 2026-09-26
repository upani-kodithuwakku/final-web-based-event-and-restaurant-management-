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

    public List<TableResponse> getAllTables() {
        return tableRepository.findAll().stream().map(this::toResponse).toList();
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
        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setLocation(request.getLocation());
        return toResponse(tableRepository.save(table));
    }

    @Transactional
    public TableResponse updateTableStatus(Long id, UpdateTableStatusRequest request) {
        RestaurantTable table = findActiveById(id);
        table.setCurrentStatus(request.getStatus());
        return toResponse(tableRepository.save(table));
    }

    @Transactional
    public void deleteTable(Long id) {
        RestaurantTable table = findActiveById(id);
        if (table.getCurrentStatus() == TableStatus.OCCUPIED || table.getCurrentStatus() == TableStatus.RESERVED) {
            throw new BadRequestException("Cannot deactivate a table that is currently reserved or occupied");
        }
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
                .location(t.getLocation())
                .currentStatus(t.getCurrentStatus())
                .isActive(t.isActive())
                .build();
    }
}
