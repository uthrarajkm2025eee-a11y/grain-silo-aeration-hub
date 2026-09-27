package com.agri.silo.service;

import com.agri.silo.dto.BudgetDTO;
import com.agri.silo.dto.BudgetVarianceDTO;
import com.agri.silo.dto.CreateBudgetRequest;
import com.agri.silo.entity.Account;
import com.agri.silo.entity.AnalyticAccount;
import com.agri.silo.entity.Budget;
import com.agri.silo.exception.ResourceNotFoundException;
import com.agri.silo.repository.AccountRepository;
import com.agri.silo.repository.AnalyticAccountRepository;
import com.agri.silo.repository.BudgetRepository;
import com.agri.silo.repository.JournalItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final AccountRepository accountRepository;
    private final AnalyticAccountRepository analyticAccountRepository;
    private final JournalItemRepository journalItemRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         AccountRepository accountRepository,
                         AnalyticAccountRepository analyticAccountRepository,
                         JournalItemRepository journalItemRepository) {
        this.budgetRepository = budgetRepository;
        this.accountRepository = accountRepository;
        this.analyticAccountRepository = analyticAccountRepository;
        this.journalItemRepository = journalItemRepository;
    }

    @Transactional(readOnly = true)
    public List<BudgetDTO> getAllBudgets() {
        return budgetRepository.findAllWithDetails().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BudgetDTO createBudget(CreateBudgetRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getAccountId()));

        AnalyticAccount analytic = analyticAccountRepository.findById(request.getAnalyticAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Analytic account not found: " + request.getAnalyticAccountId()));

        Budget budget = Budget.builder()
                .name(request.getName())
                .allocatedAmount(request.getAllocatedAmount())
                .account(account)
                .analyticAccount(analytic)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Budget saved = budgetRepository.save(budget);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public BudgetVarianceDTO getBudgetVariance(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + budgetId));

        BigDecimal actual = journalItemRepository.getActualBudgetSpend(
                budget.getAccount().getId(),
                budget.getAnalyticAccount().getId(),
                budget.getStartDate(),
                budget.getEndDate()
        );

        if (actual == null) actual = BigDecimal.ZERO;

        BigDecimal allocated = budget.getAllocatedAmount();
        BigDecimal variance = actual.subtract(allocated);

        double variancePct = 0.0;
        if (allocated.compareTo(BigDecimal.ZERO) > 0) {
            variancePct = variance.divide(allocated, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }

        return BudgetVarianceDTO.builder()
                .budgetId(budget.getId())
                .name(budget.getName())
                .analyticAccountName(budget.getAnalyticAccount().getName())
                .accountName(budget.getAccount().getName())
                .allocatedAmount(allocated)
                .actualAmount(actual)
                .varianceAmount(variance)
                .variancePercent(variancePct)
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .build();
    }

    @Transactional(readOnly = true)
    public List<BudgetVarianceDTO> getAllBudgetVariances() {
        List<Budget> budgets = budgetRepository.findAllWithDetails();
        return budgets.stream().map(b -> getBudgetVariance(b.getId())).collect(Collectors.toList());
    }

    private BudgetDTO mapToDTO(Budget b) {
        return BudgetDTO.builder()
                .id(b.getId())
                .name(b.getName())
                .allocatedAmount(b.getAllocatedAmount())
                .accountId(b.getAccount().getId())
                .accountCode(b.getAccount().getCode())
                .accountName(b.getAccount().getName())
                .analyticAccountId(b.getAnalyticAccount().getId())
                .analyticAccountCode(b.getAnalyticAccount().getCode())
                .analyticAccountName(b.getAnalyticAccount().getName())
                .startDate(b.getStartDate())
                .endDate(b.getEndDate())
                .build();
    }
}
