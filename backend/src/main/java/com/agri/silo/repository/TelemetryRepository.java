package com.agri.silo.repository;

import com.agri.silo.entity.SiloTelemetry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TelemetryRepository extends JpaRepository<SiloTelemetry, Long> {

    @Query(value = "SELECT t FROM SiloTelemetry t JOIN FETCH t.silo s WHERE s.id = :siloId ORDER BY t.timestamp DESC",
           countQuery = "SELECT COUNT(t) FROM SiloTelemetry t WHERE t.silo.id = :siloId")
    Page<SiloTelemetry> findBySiloIdOrderByTimestampDesc(@Param("siloId") Long siloId, Pageable pageable);

    @Query(value = "SELECT t FROM SiloTelemetry t JOIN FETCH t.silo s ORDER BY t.timestamp DESC",
           countQuery = "SELECT COUNT(t) FROM SiloTelemetry t")
    Page<SiloTelemetry> findAllPaged(Pageable pageable);
}
