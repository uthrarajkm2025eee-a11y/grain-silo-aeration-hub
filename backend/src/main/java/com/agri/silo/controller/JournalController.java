package com.agri.silo.controller;

import com.agri.silo.dto.CreateJournalEntryRequest;
import com.agri.silo.dto.JournalEntryDTO;
import com.agri.silo.service.AccountingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/journal-entries")
public class JournalController {

    private final AccountingService accountingService;

    public JournalController(AccountingService accountingService) {
        this.accountingService = accountingService;
    }

    @GetMapping
    public ResponseEntity<Page<JournalEntryDTO>> getJournalEntries(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(accountingService.getJournalEntriesPaged(page, size));
    }

    @PostMapping
    public ResponseEntity<JournalEntryDTO> createJournalEntry(@Valid @RequestBody CreateJournalEntryRequest request) {
        JournalEntryDTO created = accountingService.createJournalEntry(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
