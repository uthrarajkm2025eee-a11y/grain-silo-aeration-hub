package com.agri.silo.repository;

import com.agri.silo.entity.SiloUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SiloRepository extends JpaRepository<SiloUnit, Long> {
    Optional<SiloUnit> findByCode(String code);
}
