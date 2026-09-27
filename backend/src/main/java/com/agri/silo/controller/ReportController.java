package com.agri.silo.controller;

import com.agri.silo.dto.BalanceSheetReportDTO;
import com.agri.silo.dto.BudgetVarianceDTO;
import com.agri.silo.dto.ProfitAndLossReportDTO;
import com.agri.silo.service.BudgetService;
import com.agri.silo.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final BudgetService budgetService;

    public ReportController(ReportService reportService, BudgetService budgetService) {
        this.reportService = reportService;
        this.budgetService = budgetService;
    }

    @GetMapping("/balance-sheet")
    public ResponseEntity<BalanceSheetReportDTO> getBalanceSheet() {
        return ResponseEntity.ok(reportService.getBalanceSheet());
    }

    @GetMapping("/profit-and-loss")
    public ResponseEntity<ProfitAndLossReportDTO> getProfitAndLoss(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getProfitAndLoss(from, to));
    }

    @GetMapping("/budget-summary")
    public ResponseEntity<List<BudgetVarianceDTO>> getBudgetSummary() {
        return ResponseEntity.ok(budgetService.getAllBudgetVariances());
    }
}
