package com.agri.silo.service;

import com.agri.silo.dto.BalanceSheetReportDTO;
import com.agri.silo.dto.ProfitAndLossReportDTO;
import com.agri.silo.entity.Account;
import com.agri.silo.enums.AccountType;
import com.agri.silo.repository.AccountRepository;
import com.agri.silo.repository.JournalItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private final AccountRepository accountRepository;
    private final JournalItemRepository journalItemRepository;

    public ReportService(AccountRepository accountRepository, JournalItemRepository journalItemRepository) {
        this.accountRepository = accountRepository;
        this.journalItemRepository = journalItemRepository;
    }

    @Transactional(readOnly = true)
    public BalanceSheetReportDTO getBalanceSheet() {
        List<Account> assetAccounts = accountRepository.findByType(AccountType.ASSET);
        List<Account> liabilityAccounts = accountRepository.findByType(AccountType.LIABILITY);

        BigDecimal totalAssets = BigDecimal.ZERO;
        List<BalanceSheetReportDTO.AccountBalanceItem> assetItems = new ArrayList<>();

        for (Account a : assetAccounts) {
            BigDecimal bal = journalItemRepository.getNetAssetBalance(a.getId());
            if (bal == null) bal = BigDecimal.ZERO;
            assetItems.add(new BalanceSheetReportDTO.AccountBalanceItem(a.getCode(), a.getName(), bal));
            totalAssets = totalAssets.add(bal);
        }

        BigDecimal totalLiabilities = BigDecimal.ZERO;
        List<BalanceSheetReportDTO.AccountBalanceItem> liabilityItems = new ArrayList<>();

        for (Account l : liabilityAccounts) {
            BigDecimal bal = journalItemRepository.getNetLiabilityBalance(l.getId());
            if (bal == null) bal = BigDecimal.ZERO;
            liabilityItems.add(new BalanceSheetReportDTO.AccountBalanceItem(l.getCode(), l.getName(), bal));
            totalLiabilities = totalLiabilities.add(bal);
        }

        BigDecimal netEquity = totalAssets.subtract(totalLiabilities);

        return BalanceSheetReportDTO.builder()
                .totalAssets(totalAssets)
                .assets(assetItems)
                .totalLiabilities(totalLiabilities)
                .liabilities(liabilityItems)
                .netEquity(netEquity)
                .build();
    }

    @Transactional(readOnly = true)
    public ProfitAndLossReportDTO getProfitAndLoss(LocalDate fromDate, LocalDate toDate) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate to = toDate != null ? toDate : LocalDate.now();

        List<Account> incomeAccounts = accountRepository.findByType(AccountType.INCOME);
        List<Account> expenseAccounts = accountRepository.findByType(AccountType.EXPENSE);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        List<ProfitAndLossReportDTO.IncomeExpenseItem> revenueItems = new ArrayList<>();

        for (Account inc : incomeAccounts) {
            BigDecimal amt = journalItemRepository.getIncomeBalanceDateRange(inc.getId(), from, to);
            if (amt == null) amt = BigDecimal.ZERO;
            revenueItems.add(new ProfitAndLossReportDTO.IncomeExpenseItem(inc.getCode(), inc.getName(), amt));
            totalRevenue = totalRevenue.add(amt);
        }

        BigDecimal totalExpenses = BigDecimal.ZERO;
        List<ProfitAndLossReportDTO.IncomeExpenseItem> expenseItems = new ArrayList<>();

        for (Account exp : expenseAccounts) {
            BigDecimal amt = journalItemRepository.getExpenseBalanceDateRange(exp.getId(), from, to);
            if (amt == null) amt = BigDecimal.ZERO;
            expenseItems.add(new ProfitAndLossReportDTO.IncomeExpenseItem(exp.getCode(), exp.getName(), amt));
            totalExpenses = totalExpenses.add(amt);
        }

        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);

        return ProfitAndLossReportDTO.builder()
                .fromDate(from)
                .toDate(to)
                .totalRevenue(totalRevenue)
                .revenues(revenueItems)
                .totalExpenses(totalExpenses)
                .expenses(expenseItems)
                .netProfit(netProfit)
                .build();
    }
}
