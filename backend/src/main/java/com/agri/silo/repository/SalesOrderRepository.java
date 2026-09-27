package com.agri.silo.repository;

import com.agri.silo.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    @Query(value = "SELECT DISTINCT so FROM SalesOrder so JOIN FETCH so.customer c LEFT JOIN FETCH so.lines l LEFT JOIN FETCH l.product p ORDER BY so.id DESC",
           countQuery = "SELECT COUNT(so) FROM SalesOrder so")
    Page<SalesOrder> findAllPaged(Pageable pageable);
}
