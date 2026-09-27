package com.agri.silo.repository;

import com.agri.silo.entity.VendorBill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VendorBillRepository extends JpaRepository<VendorBill, Long> {

    @Query(value = "SELECT vb FROM VendorBill vb JOIN FETCH vb.vendor v LEFT JOIN FETCH vb.purchaseOrder po ORDER BY vb.id DESC",
           countQuery = "SELECT COUNT(vb) FROM VendorBill vb")
    Page<VendorBill> findAllPaged(Pageable pageable);
}
