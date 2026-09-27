package com.agri.silo.controller;

import com.agri.silo.dto.BudgetDTO;
import com.agri.silo.dto.BudgetVarianceDTO;
import com.agri.silo.dto.CreateBudgetRequest;
import com.agri.silo.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public ResponseEntity<List<BudgetDTO>> getAllBudgets() {
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }

    @PostMapping
    public ResponseEntity<BudgetDTO> createBudget(@Valid @RequestBody CreateBudgetRequest request) {
        BudgetDTO created = budgetService.createBudget(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/variance")
    public ResponseEntity<BudgetVarianceDTO> getBudgetVariance(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetVariance(id));
    }

    @GetMapping("/variances")
    public ResponseEntity<List<BudgetVarianceDTO>> getAllBudgetVariances() {
        return ResponseEntity.ok(budgetService.getAllBudgetVariances());
    }
}
