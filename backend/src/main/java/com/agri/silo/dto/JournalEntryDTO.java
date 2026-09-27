package com.agri.silo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryDTO {
    private Long id;
    private String entryNumber;
    private Long journalId;
    private String journalCode;
    private String journalName;
    private LocalDate entryDate;
    private String reference;
    private String state;
    private List<JournalItemDTO> items;
}
