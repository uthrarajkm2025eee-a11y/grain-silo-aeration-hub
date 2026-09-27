package com.agri.silo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetVarianceDTO {
    private Long budgetId;
    private String name;
    private String analyticAccountName;
    private String accountName;
    private BigDecimal allocatedAmount;
    private BigDecimal actualAmount;
    private BigDecimal varianceAmount;
    private Double variancePercent; // (actual - allocated) / allocated * 100
    private LocalDate startDate;
    private LocalDate endDate;
}
