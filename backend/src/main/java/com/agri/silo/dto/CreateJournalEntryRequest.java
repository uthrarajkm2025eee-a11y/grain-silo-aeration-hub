package com.agri.silo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateJournalEntryRequest {
    @NotNull(message = "Journal ID is required")
    private Long journalId;

    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;

    private String reference;

    @NotEmpty(message = "Journal items are required")
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        @NotNull(message = "Account ID is required")
        private Long accountId;

        private Long analyticAccountId;
        private String label;
        private BigDecimal debit = BigDecimal.ZERO;
        private BigDecimal credit = BigDecimal.ZERO;
    }
}
