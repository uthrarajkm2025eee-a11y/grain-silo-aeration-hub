package com.agri.silo.repository;

import com.agri.silo.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query(value = "SELECT DISTINCT po FROM PurchaseOrder po JOIN FETCH po.vendor v LEFT JOIN FETCH po.lines l LEFT JOIN FETCH l.product p ORDER BY po.id DESC",
           countQuery = "SELECT COUNT(po) FROM PurchaseOrder po")
    Page<PurchaseOrder> findAllPaged(Pageable pageable);
}
