package com.agri.silo.dto;

import com.agri.silo.enums.PaymentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreatePaymentRequest {
    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    @NotNull(message = "Partner ID is required")
    private Long partnerId;

    @NotNull(message = "Bank account ID is required")
    private Long bankAccountId;

    private Long billId;
    private Long invoiceId;
}
