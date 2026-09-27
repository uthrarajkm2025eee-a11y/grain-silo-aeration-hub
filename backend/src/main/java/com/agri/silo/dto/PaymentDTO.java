package com.agri.silo.dto;

import com.agri.silo.enums.PaymentType;
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
public class PaymentDTO {
    private Long id;
    private String paymentNumber;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private PaymentType paymentType;
    private Long partnerId;
    private String partnerName;
    private Long bankAccountId;
    private String bankAccountName;
    private Long billId;
    private String billNumber;
    private Long invoiceId;
    private String invoiceNumber;
    private Long journalEntryId;
}
