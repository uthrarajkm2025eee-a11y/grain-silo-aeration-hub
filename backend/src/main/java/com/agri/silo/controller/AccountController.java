package com.agri.silo.controller;

import com.agri.silo.dto.AccountDTO;
import com.agri.silo.dto.AnalyticAccountDTO;
import com.agri.silo.dto.JournalDTO;
import com.agri.silo.service.AccountingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final AccountingService accountingService;

    public AccountController(AccountingService accountingService) {
        this.accountingService = accountingService;
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountingService.getAllAccounts());
    }

    @GetMapping("/analytic-accounts")
    public ResponseEntity<List<AnalyticAccountDTO>> getAllAnalyticAccounts() {
        return ResponseEntity.ok(accountingService.getAllAnalyticAccounts());
    }

    @GetMapping("/journals")
    public ResponseEntity<List<JournalDTO>> getAllJournals() {
        return ResponseEntity.ok(accountingService.getAllJournals());
    }
}
