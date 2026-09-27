package com.agri.silo.controller;

import com.agri.silo.dto.CreatePaymentRequest;
import com.agri.silo.dto.PaymentDTO;
import com.agri.silo.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<Page<PaymentDTO>> getPayments(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(paymentService.getPaymentsPaged(page, size));
    }

    @PostMapping
    public ResponseEntity<PaymentDTO> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentDTO created = paymentService.createPayment(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
