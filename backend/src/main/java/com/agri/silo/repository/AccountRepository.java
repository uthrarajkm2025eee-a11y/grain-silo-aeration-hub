package com.agri.silo.repository;

import com.agri.silo.entity.Account;
import com.agri.silo.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCode(String code);
    List<Account> findByType(AccountType type);
}
