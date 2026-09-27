package com.agri.silo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceSheetReportDTO {
    private BigDecimal totalAssets;
    private List<AccountBalanceItem> assets;
    private BigDecimal totalLiabilities;
    private List<AccountBalanceItem> liabilities;
    private BigDecimal netEquity;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccountBalanceItem {
        private String code;
        private String name;
        private BigDecimal balance;
    }
}
