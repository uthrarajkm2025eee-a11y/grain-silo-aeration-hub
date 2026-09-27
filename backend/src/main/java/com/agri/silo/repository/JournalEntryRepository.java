package com.agri.silo.repository;

import com.agri.silo.entity.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    @Query(value = "SELECT DISTINCT e FROM JournalEntry e JOIN FETCH e.journal j LEFT JOIN FETCH e.items i LEFT JOIN FETCH i.account a ORDER BY e.entryDate DESC, e.id DESC",
           countQuery = "SELECT COUNT(e) FROM JournalEntry e")
    Page<JournalEntry> findAllPaged(Pageable pageable);
}
