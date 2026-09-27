package com.agri.silo.repository;

import com.agri.silo.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("SELECT b FROM Budget b JOIN FETCH b.account a JOIN FETCH b.analyticAccount aa")
    List<Budget> findAllWithDetails();
}
