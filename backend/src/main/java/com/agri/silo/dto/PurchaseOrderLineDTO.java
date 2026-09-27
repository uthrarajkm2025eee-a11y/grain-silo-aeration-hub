package com.agri.silo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderLineDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productCode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private BigDecimal subtotal;
}
