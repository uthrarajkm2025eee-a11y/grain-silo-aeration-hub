package com.agri.silo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateBudgetRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Allocated amount is required")
    @Min(value = 0, message = "Allocated amount must be non-negative")
    private BigDecimal allocatedAmount;

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotNull(message = "Analytic account ID is required")
    private Long analyticAccountId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}
