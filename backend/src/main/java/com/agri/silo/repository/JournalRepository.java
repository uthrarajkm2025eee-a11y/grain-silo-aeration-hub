package com.agri.silo.repository;

import com.agri.silo.entity.Journal;
import com.agri.silo.enums.JournalType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JournalRepository extends JpaRepository<Journal, Long> {
    Optional<Journal> findByCode(String code);
    Optional<Journal> findByType(JournalType type);
}
