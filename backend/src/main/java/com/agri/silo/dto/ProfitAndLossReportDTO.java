package com.agri.silo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitAndLossReportDTO {
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal totalRevenue;
    private List<IncomeExpenseItem> revenues;
    private BigDecimal totalExpenses;
    private List<IncomeExpenseItem> expenses;
    private BigDecimal netProfit;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IncomeExpenseItem {
        private String code;
        private String name;
        private BigDecimal amount;
    }
}
