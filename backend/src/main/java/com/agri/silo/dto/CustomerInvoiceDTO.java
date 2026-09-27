package com.agri.silo.dto;

import com.agri.silo.enums.InvoiceStatus;
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
public class CustomerInvoiceDTO {
    private Long id;
    private String invoiceNumber;
    private Long soId;
    private String soNumber;
    private Long customerId;
    private String customerName;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
    private Long journalEntryId;
}
