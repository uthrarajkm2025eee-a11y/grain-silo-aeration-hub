package com.agri.silo.repository;

import com.agri.silo.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query(value = "SELECT p FROM Payment p JOIN FETCH p.partner pr JOIN FETCH p.bankAccount b LEFT JOIN FETCH p.bill LEFT JOIN FETCH p.invoice ORDER BY p.id DESC",
           countQuery = "SELECT COUNT(p) FROM Payment p")
    Page<Payment> findAllPaged(Pageable pageable);
}
