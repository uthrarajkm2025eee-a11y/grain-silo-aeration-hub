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
public class BudgetDTO {
    private Long id;
    private String name;
    private BigDecimal allocatedAmount;
    private Long accountId;
    private String accountCode;
    private String accountName;
    private Long analyticAccountId;
    private String analyticAccountCode;
    private String analyticAccountName;
    private LocalDate startDate;
    private LocalDate endDate;
}
