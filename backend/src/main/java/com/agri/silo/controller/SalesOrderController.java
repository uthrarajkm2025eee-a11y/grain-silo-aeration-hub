package com.agri.silo.controller;

import com.agri.silo.dto.CreateSalesOrderRequest;
import com.agri.silo.dto.CustomerInvoiceDTO;
import com.agri.silo.dto.SalesOrderDTO;
import com.agri.silo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SalesOrderController {

    private final OrderService orderService;

    public SalesOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/sales-orders")
    public ResponseEntity<Page<SalesOrderDTO>> getSalesOrders(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getSalesOrdersPaged(page, size));
    }

    @PostMapping("/sales-orders")
    public ResponseEntity<SalesOrderDTO> createSalesOrder(@Valid @RequestBody CreateSalesOrderRequest request) {
        SalesOrderDTO created = orderService.createSalesOrder(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/sales-orders/{id}/convert-to-invoice")
    public ResponseEntity<CustomerInvoiceDTO> convertToInvoice(@PathVariable("id") Long soId) {
        CustomerInvoiceDTO invoice = orderService.convertSOToInvoice(soId);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/customer-invoices")
    public ResponseEntity<Page<CustomerInvoiceDTO>> getCustomerInvoices(@RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getCustomerInvoicesPaged(page, size));
    }
}
