package com.agri.silo.repository;

import com.agri.silo.entity.FanEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FanEventRepository extends JpaRepository<FanEvent, Long> {

    @Query("SELECT f FROM FanEvent f JOIN FETCH f.silo s WHERE s.id = :siloId ORDER BY f.timestamp DESC")
    List<FanEvent> findBySiloIdOrderByTimestampDesc(@Param("siloId") Long siloId);

    @Query(value = "SELECT f FROM FanEvent f JOIN FETCH f.silo s ORDER BY f.timestamp DESC",
           countQuery = "SELECT COUNT(f) FROM FanEvent f")
    Page<FanEvent> findAllPaged(Pageable pageable);
}
