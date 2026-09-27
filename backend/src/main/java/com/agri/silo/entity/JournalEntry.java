package com.agri.silo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journal_entries", indexes = {
    @Index(name = "idx_entry_date", columnList = "entry_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_number", nullable = false, unique = true, length = 50)
    private String entryNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_id", nullable = false)
    private Journal journal;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(length = 100)
    private String reference;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String state = "POSTED";

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<JournalItem> items = new ArrayList<>();

    public void addItem(JournalItem item) {
        items.add(item);
        item.setEntry(this);
    }
}
