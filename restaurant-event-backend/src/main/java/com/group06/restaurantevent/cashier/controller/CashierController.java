package com.group06.restaurantevent.cashier.controller;

import com.group06.restaurantevent.cashier.dto.response.CashierSummaryResponse;
import com.group06.restaurantevent.cashier.service.CashierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cashier")
@RequiredArgsConstructor
public class CashierController {
    private final CashierService service;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('CASHIER','ADMIN','MANAGER')")
    public CashierSummaryResponse summary() { return service.summary(); }
}
