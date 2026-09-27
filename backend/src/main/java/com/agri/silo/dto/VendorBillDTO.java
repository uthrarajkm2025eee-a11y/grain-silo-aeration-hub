package com.agri.silo.dto;

import com.agri.silo.enums.BillStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorBillDTO {
    private Long id;
    private String billNumber;
    private Long poId;
    private String poNumber;
    private Long vendorId;
    private String vendorName;
    private LocalDate billDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BillStatus status;
    private Long journalEntryId;
}
