package com.agri.silo.dto;

import com.agri.silo.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private Long id;
    private String code;
    private String name;
    private AccountType type;
    private Long parentId;
    private String parentName;
    private BigDecimal balance;
}
