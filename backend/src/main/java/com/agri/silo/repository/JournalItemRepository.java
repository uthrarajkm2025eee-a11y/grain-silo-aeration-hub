package com.agri.silo.repository;

import com.agri.silo.entity.JournalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface JournalItemRepository extends JpaRepository<JournalItem, Long> {

    @Query("SELECT COALESCE(SUM(ji.debit - ji.credit), 0) FROM JournalItem ji WHERE ji.account.id = :accountId")
    BigDecimal getNetAssetBalance(@Param("accountId") Long accountId);

    @Query("SELECT COALESCE(SUM(ji.credit - ji.debit), 0) FROM JournalItem ji WHERE ji.account.id = :accountId")
    BigDecimal getNetLiabilityBalance(@Param("accountId") Long accountId);

    @Query("SELECT COALESCE(SUM(ji.credit - ji.debit), 0) FROM JournalItem ji WHERE ji.account.id = :accountId AND ji.entry.entryDate BETWEEN :fromDate AND :toDate")
    BigDecimal getIncomeBalanceDateRange(@Param("accountId") Long accountId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query("SELECT COALESCE(SUM(ji.debit - ji.credit), 0) FROM JournalItem ji WHERE ji.account.id = :accountId AND ji.entry.entryDate BETWEEN :fromDate AND :toDate")
    BigDecimal getExpenseBalanceDateRange(@Param("accountId") Long accountId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query("SELECT COALESCE(SUM(ji.debit - ji.credit), 0) FROM JournalItem ji WHERE ji.account.id = :accountId AND ji.analyticAccount.id = :analyticAccountId AND ji.entry.entryDate BETWEEN :startDate AND :endDate")
    BigDecimal getActualBudgetSpend(@Param("accountId") Long accountId, @Param("analyticAccountId") Long analyticAccountId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
