package com.agri.silo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "journal_items", indexes = {
    @Index(name = "idx_item_entry", columnList = "entry_id"),
    @Index(name = "idx_item_account", columnList = "account_id"),
    @Index(name = "idx_item_analytic", columnList = "analytic_account_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entry_id", nullable = false)
    private JournalEntry entry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analytic_account_id")
    private AnalyticAccount analyticAccount;

    @Column(length = 200)
    private String label;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal debit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal credit = BigDecimal.ZERO;
}
