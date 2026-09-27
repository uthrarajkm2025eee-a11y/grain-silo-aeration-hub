package com.agri.silo.repository;

import com.agri.silo.entity.CustomerInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerInvoiceRepository extends JpaRepository<CustomerInvoice, Long> {

    @Query(value = "SELECT ci FROM CustomerInvoice ci JOIN FETCH ci.customer c LEFT JOIN FETCH ci.salesOrder so ORDER BY ci.id DESC",
           countQuery = "SELECT COUNT(ci) FROM CustomerInvoice ci")
    Page<CustomerInvoice> findAllPaged(Pageable pageable);
}
