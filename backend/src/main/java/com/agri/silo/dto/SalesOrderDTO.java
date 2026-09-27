package com.agri.silo.dto;

import com.agri.silo.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderDTO {
    private Long id;
    private String soNumber;
    private Long customerId;
    private String customerName;
    private LocalDate orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<SalesOrderLineDTO> lines;
}
