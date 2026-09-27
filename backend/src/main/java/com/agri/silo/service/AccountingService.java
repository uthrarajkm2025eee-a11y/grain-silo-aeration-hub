package com.agri.silo.service;

import com.agri.silo.dto.*;
import com.agri.silo.entity.*;
import com.agri.silo.enums.AccountType;
import com.agri.silo.exception.InvalidAccountingException;
import com.agri.silo.exception.ResourceNotFoundException;
import com.agri.silo.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountingService {

    private final AccountRepository accountRepository;
    private final AnalyticAccountRepository analyticAccountRepository;
    private final JournalRepository journalRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalItemRepository journalItemRepository;

    public AccountingService(AccountRepository accountRepository,
                             AnalyticAccountRepository analyticAccountRepository,
                             JournalRepository journalRepository,
                             JournalEntryRepository journalEntryRepository,
                             JournalItemRepository journalItemRepository) {
        this.accountRepository = accountRepository;
        this.analyticAccountRepository = analyticAccountRepository;
        this.journalRepository = journalRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.journalItemRepository = journalItemRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::mapToAccountDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnalyticAccountDTO> getAllAnalyticAccounts() {
        return analyticAccountRepository.findAll().stream()
                .map(a -> AnalyticAccountDTO.builder()
                        .id(a.getId())
                        .code(a.getCode())
                        .name(a.getName())
                        .description(a.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JournalDTO> getAllJournals() {
        return journalRepository.findAll().stream()
                .map(j -> JournalDTO.builder()
                        .id(j.getId())
                        .code(j.getCode())
                        .name(j.getName())
                        .type(j.getType())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<JournalEntryDTO> getJournalEntriesPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return journalEntryRepository.findAllPaged(pageable).map(this::mapToEntryDTO);
    }

    @Transactional
    public JournalEntryDTO createJournalEntry(CreateJournalEntryRequest request) {
        Journal journal = journalRepository.findById(request.getJournalId())
                .orElseThrow(() -> new ResourceNotFoundException("Journal not found"));

        // Validate double-entry balance
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (CreateJournalEntryRequest.ItemRequest item : request.getItems()) {
            if (item.getDebit() != null) totalDebit = totalDebit.add(item.getDebit());
            if (item.getCredit() != null) totalCredit = totalCredit.add(item.getCredit());
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new InvalidAccountingException(String.format(
                    "Journal Entry unbalanced: Total Debits (%s) must equal Total Credits (%s)", totalDebit, totalCredit));
        }

        String entryNum = "JE/" + request.getEntryDate().getYear() + "/" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        JournalEntry entry = JournalEntry.builder()
                .entryNumber(entryNum)
                .journal(journal)
                .entryDate(request.getEntryDate())
                .reference(request.getReference())
                .state("POSTED")
                .createdAt(LocalDateTime.now())
                .build();

        for (CreateJournalEntryRequest.ItemRequest itemReq : request.getItems()) {
            Account account = accountRepository.findById(itemReq.getAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + itemReq.getAccountId()));

            AnalyticAccount analytic = null;
            if (itemReq.getAnalyticAccountId() != null) {
                analytic = analyticAccountRepository.findById(itemReq.getAnalyticAccountId()).orElse(null);
            }

            JournalItem item = JournalItem.builder()
                    .account(account)
                    .analyticAccount(analytic)
                    .label(itemReq.getLabel())
                    .debit(itemReq.getDebit() != null ? itemReq.getDebit() : BigDecimal.ZERO)
                    .credit(itemReq.getCredit() != null ? itemReq.getCredit() : BigDecimal.ZERO)
                    .build();

            entry.addItem(item);
        }

        JournalEntry saved = journalEntryRepository.save(entry);
        return mapToEntryDTO(saved);
    }

    private AccountDTO mapToAccountDTO(Account a) {
        BigDecimal balance;
        if (a.getType() == AccountType.ASSET || a.getType() == AccountType.EXPENSE) {
            balance = journalItemRepository.getNetAssetBalance(a.getId());
        } else {
            balance = journalItemRepository.getNetLiabilityBalance(a.getId());
        }

        return AccountDTO.builder()
                .id(a.getId())
                .code(a.getCode())
                .name(a.getName())
                .type(a.getType())
                .parentId(a.getParent() != null ? a.getParent().getId() : null)
                .parentName(a.getParent() != null ? a.getParent().getName() : null)
                .balance(balance)
                .build();
    }

    public JournalEntryDTO mapToEntryDTO(JournalEntry e) {
        List<JournalItemDTO> itemDTOs = e.getItems().stream().map(i -> JournalItemDTO.builder()
                .id(i.getId())
                .accountId(i.getAccount().getId())
                .accountCode(i.getAccount().getCode())
                .accountName(i.getAccount().getName())
                .analyticAccountId(i.getAnalyticAccount() != null ? i.getAnalyticAccount().getId() : null)
                .analyticAccountName(i.getAnalyticAccount() != null ? i.getAnalyticAccount().getName() : null)
                .label(i.getLabel())
                .debit(i.getDebit())
                .credit(i.getCredit())
                .build()
        ).collect(Collectors.toList());

        return JournalEntryDTO.builder()
                .id(e.getId())
                .entryNumber(e.getEntryNumber())
                .journalId(e.getJournal().getId())
                .journalCode(e.getJournal().getCode())
                .journalName(e.getJournal().getName())
                .entryDate(e.getEntryDate())
                .reference(e.getReference())
                .state(e.getState())
                .items(itemDTOs)
                .build();
    }
}
