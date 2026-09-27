package com.agri.silo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalItemDTO {
    private Long id;
    private Long accountId;
    private String accountCode;
    private String accountName;
    private Long analyticAccountId;
    private String analyticAccountName;
    private String label;
    private BigDecimal debit;
    private BigDecimal credit;
}
