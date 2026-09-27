package com.agri.silo.controller;

import com.agri.silo.dto.CreatePurchaseOrderRequest;
import com.agri.silo.dto.PurchaseOrderDTO;
import com.agri.silo.dto.VendorBillDTO;
import com.agri.silo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PurchaseOrderController {

    private final OrderService orderService;

    public PurchaseOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/purchase-orders")
    public ResponseEntity<Page<PurchaseOrderDTO>> getPurchaseOrders(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getPurchaseOrdersPaged(page, size));
    }

    @PostMapping("/purchase-orders")
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        PurchaseOrderDTO created = orderService.createPurchaseOrder(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/purchase-orders/{id}/convert-to-bill")
    public ResponseEntity<VendorBillDTO> convertToBill(@PathVariable("id") Long poId) {
        VendorBillDTO bill = orderService.convertPOToBill(poId);
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/vendor-bills")
    public ResponseEntity<Page<VendorBillDTO>> getVendorBills(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderService.getVendorBillsPaged(page, size));
    }
}
