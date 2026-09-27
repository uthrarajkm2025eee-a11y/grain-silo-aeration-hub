package com.agri.silo.service;

import com.agri.silo.dto.*;
import com.agri.silo.entity.*;
import com.agri.silo.enums.BillStatus;
import com.agri.silo.enums.InvoiceStatus;
import com.agri.silo.enums.OrderStatus;
import com.agri.silo.exception.ResourceNotFoundException;
import com.agri.silo.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final PurchaseOrderRepository poRepository;
    private final VendorBillRepository billRepository;
    private final SalesOrderRepository soRepository;
    private final CustomerInvoiceRepository invoiceRepository;
    private final ContactRepository contactRepository;
    private final ProductRepository productRepository;
    private final JournalRepository journalRepository;
    private final AccountRepository accountRepository;
    private final AccountingService accountingService;

    public OrderService(PurchaseOrderRepository poRepository,
                        VendorBillRepository billRepository,
                        SalesOrderRepository soRepository,
                        CustomerInvoiceRepository invoiceRepository,
                        ContactRepository contactRepository,
                        ProductRepository productRepository,
                        JournalRepository journalRepository,
                        AccountRepository accountRepository,
                        AccountingService accountingService) {
        this.poRepository = poRepository;
        this.billRepository = billRepository;
        this.soRepository = soRepository;
        this.invoiceRepository = invoiceRepository;
        this.contactRepository = contactRepository;
        this.productRepository = productRepository;
        this.journalRepository = journalRepository;
        this.accountRepository = accountRepository;
        this.accountingService = accountingService;
    }

    // --- PURCHASE ORDERS ---

    @Transactional(readOnly = true)
    public Page<PurchaseOrderDTO> getPurchaseOrdersPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return poRepository.findAllPaged(pageable).map(this::mapToPODTO);
    }

    @Transactional
    public PurchaseOrderDTO createPurchaseOrder(CreatePurchaseOrderRequest request) {
        Contact vendor = contactRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        String poNum = "PO/" + LocalDate.now().getYear() + "/" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(poNum)
                .vendor(vendor)
                .orderDate(request.getOrderDate())
                .status(OrderStatus.CONFIRMED)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CreatePurchaseOrderRequest.OrderLineItem itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.getProductId()));

            BigDecimal qty = BigDecimal.valueOf(itemReq.getQuantity());
            BigDecimal lineTotal = product.getUnitPrice().multiply(qty);
            if (product.getTaxRate() != null && product.getTaxRate().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal taxMultiplier = BigDecimal.ONE.add(product.getTaxRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
                lineTotal = lineTotal.multiply(taxMultiplier).setScale(2, RoundingMode.HALF_UP);
            }

            PurchaseOrderLine line = PurchaseOrderLine.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getUnitPrice())
                    .taxRate(product.getTaxRate())
                    .subtotal(lineTotal)
                    .build();

            po.addLine(line);
            total = total.add(lineTotal);
        }

        po.setTotalAmount(total);
        PurchaseOrder saved = poRepository.save(po);
        return mapToPODTO(saved);
    }

    @Transactional
    public VendorBillDTO convertPOToBill(Long poId) {
        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found: " + poId));

        po.setStatus(OrderStatus.BILLED);

        String billNum = "BILL/" + LocalDate.now().getYear() + "/" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Find Purchase Journal
        Journal purJournal = journalRepository.findByCode("PUR")
                .orElseGet(() -> journalRepository.findAll().get(0));

        // Create Journal Entry
        // Debit Expense Account (4020 Sensor Maintenance / 4010 Fan Electricity Costs), Credit Vendor Payables (2010)
        Account expenseAcc = accountRepository.findByCode("4020")
                .orElseGet(() -> accountRepository.findByCode("4010")
                        .orElseThrow(() -> new ResourceNotFoundException("Expense account not found")));

        Account payableAcc = accountRepository.findByCode("2010")
                .orElseThrow(() -> new ResourceNotFoundException("Vendor Payables account (2010) not found"));

        CreateJournalEntryRequest jeReq = new CreateJournalEntryRequest();
        jeReq.setJournalId(purJournal.getId());
        jeReq.setEntryDate(LocalDate.now());
        jeReq.setReference("Vendor Bill for PO " + po.getPoNumber());

        List<CreateJournalEntryRequest.ItemRequest> items = new ArrayList<>();

        CreateJournalEntryRequest.ItemRequest debitItem = new CreateJournalEntryRequest.ItemRequest();
        debitItem.setAccountId(expenseAcc.getId());
        debitItem.setLabel("Expense - PO " + po.getPoNumber());
        debitItem.setDebit(po.getTotalAmount());
        debitItem.setCredit(BigDecimal.ZERO);
        items.add(debitItem);

        CreateJournalEntryRequest.ItemRequest creditItem = new CreateJournalEntryRequest.ItemRequest();
        creditItem.setAccountId(payableAcc.getId());
        creditItem.setLabel("Vendor Payable - " + po.getVendor().getName());
        creditItem.setDebit(BigDecimal.ZERO);
        creditItem.setCredit(po.getTotalAmount());
        items.add(creditItem);

        jeReq.setItems(items);

        JournalEntryDTO jeDTO = accountingService.createJournalEntry(jeReq);

        VendorBill bill = VendorBill.builder()
                .billNumber(billNum)
                .purchaseOrder(po)
                .vendor(po.getVendor())
                .billDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .totalAmount(po.getTotalAmount())
                .status(BillStatus.UNPAID)
                .journalEntry(JournalEntry.builder().id(jeDTO.getId()).build())
                .build();

        VendorBill savedBill = billRepository.save(bill);
        poRepository.save(po);

        return mapToBillDTO(savedBill);
    }

    @Transactional(readOnly = true)
    public Page<VendorBillDTO> getVendorBillsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return billRepository.findAllPaged(pageable).map(this::mapToBillDTO);
    }

    // --- SALES ORDERS ---

    @Transactional(readOnly = true)
    public Page<SalesOrderDTO> getSalesOrdersPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return soRepository.findAllPaged(pageable).map(this::mapToSODTO);
    }

    @Transactional
    public SalesOrderDTO createSalesOrder(CreateSalesOrderRequest request) {
        Contact customer = contactRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        String soNum = "SO/" + LocalDate.now().getYear() + "/" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        SalesOrder so = SalesOrder.builder()
                .soNumber(soNum)
                .customer(customer)
                .orderDate(request.getOrderDate())
                .status(OrderStatus.CONFIRMED)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CreateSalesOrderRequest.OrderLineItem itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.getProductId()));

            BigDecimal qty = BigDecimal.valueOf(itemReq.getQuantity());
            BigDecimal lineTotal = product.getUnitPrice().multiply(qty);
            if (product.getTaxRate() != null && product.getTaxRate().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal taxMultiplier = BigDecimal.ONE.add(product.getTaxRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
                lineTotal = lineTotal.multiply(taxMultiplier).setScale(2, RoundingMode.HALF_UP);
            }

            SalesOrderLine line = SalesOrderLine.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getUnitPrice())
                    .taxRate(product.getTaxRate())
                    .subtotal(lineTotal)
                    .build();

            so.addLine(line);
            total = total.add(lineTotal);
        }

        so.setTotalAmount(total);
        SalesOrder saved = soRepository.save(so);
        return mapToSODTO(saved);
    }

    @Transactional
    public CustomerInvoiceDTO convertSOToInvoice(Long soId) {
        SalesOrder so = soRepository.findById(soId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found: " + soId));

        so.setStatus(OrderStatus.INVOICED);

        String invNum = "INV/" + LocalDate.now().getYear() + "/" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Find Sales Journal
        Journal slsJournal = journalRepository.findByCode("SLS")
                .orElseGet(() -> journalRepository.findAll().get(0));

        // Create Journal Entry
        // Debit Accounts Receivable (1040), Credit Grain Conditioning Revenue (3010)
        Account recAcc = accountRepository.findByCode("1040")
                .orElseThrow(() -> new ResourceNotFoundException("Accounts Receivable (1040) not found"));

        Account revAcc = accountRepository.findByCode("3010")
                .orElseGet(() -> accountRepository.findByCode("3020")
                        .orElseThrow(() -> new ResourceNotFoundException("Revenue account not found")));

        CreateJournalEntryRequest jeReq = new CreateJournalEntryRequest();
        jeReq.setJournalId(slsJournal.getId());
        jeReq.setEntryDate(LocalDate.now());
        jeReq.setReference("Customer Invoice for SO " + so.getSoNumber());

        List<CreateJournalEntryRequest.ItemRequest> items = new ArrayList<>();

        CreateJournalEntryRequest.ItemRequest debitItem = new CreateJournalEntryRequest.ItemRequest();
        debitItem.setAccountId(recAcc.getId());
        debitItem.setLabel("Customer Receivable - " + so.getCustomer().getName());
        debitItem.setDebit(so.getTotalAmount());
        debitItem.setCredit(BigDecimal.ZERO);
        items.add(debitItem);

        CreateJournalEntryRequest.ItemRequest creditItem = new CreateJournalEntryRequest.ItemRequest();
        creditItem.setAccountId(revAcc.getId());
        creditItem.setLabel("Revenue - SO " + so.getSoNumber());
        creditItem.setDebit(BigDecimal.ZERO);
        creditItem.setCredit(so.getTotalAmount());
        items.add(creditItem);

        jeReq.setItems(items);

        JournalEntryDTO jeDTO = accountingService.createJournalEntry(jeReq);

        CustomerInvoice invoice = CustomerInvoice.builder()
                .invoiceNumber(invNum)
                .salesOrder(so)
                .customer(so.getCustomer())
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .totalAmount(so.getTotalAmount())
                .status(InvoiceStatus.UNPAID)
                .journalEntry(JournalEntry.builder().id(jeDTO.getId()).build())
                .build();

        CustomerInvoice savedInv = invoiceRepository.save(invoice);
        soRepository.save(so);

        return mapToInvoiceDTO(savedInv);
    }

    @Transactional(readOnly = true)
    public Page<CustomerInvoiceDTO> getCustomerInvoicesPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return invoiceRepository.findAllPaged(pageable).map(this::mapToInvoiceDTO);
    }

    // Mapping Helpers
    private PurchaseOrderDTO mapToPODTO(PurchaseOrder po) {
        List<PurchaseOrderLineDTO> lineDTOs = po.getLines().stream().map(l -> PurchaseOrderLineDTO.builder()
                .id(l.getId())
                .productId(l.getProduct().getId())
                .productName(l.getProduct().getName())
                .productCode(l.getProduct().getCode())
                .quantity(l.getQuantity())
                .unitPrice(l.getUnitPrice())
                .taxRate(l.getTaxRate())
                .subtotal(l.getSubtotal())
                .build()
        ).collect(Collectors.toList());

        return PurchaseOrderDTO.builder()
                .id(po.getId())
                .poNumber(po.getPoNumber())
                .vendorId(po.getVendor().getId())
                .vendorName(po.getVendor().getName())
                .orderDate(po.getOrderDate())
                .status(po.getStatus())
                .totalAmount(po.getTotalAmount())
                .lines(lineDTOs)
                .build();
    }

    public VendorBillDTO mapToBillDTO(VendorBill bill) {
        return VendorBillDTO.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .poId(bill.getPurchaseOrder() != null ? bill.getPurchaseOrder().getId() : null)
                .poNumber(bill.getPurchaseOrder() != null ? bill.getPurchaseOrder().getPoNumber() : null)
                .vendorId(bill.getVendor().getId())
                .vendorName(bill.getVendor().getName())
                .billDate(bill.getBillDate())
                .dueDate(bill.getDueDate())
                .totalAmount(bill.getTotalAmount())
                .status(bill.getStatus())
                .journalEntryId(bill.getJournalEntry() != null ? bill.getJournalEntry().getId() : null)
                .build();
    }

    private SalesOrderDTO mapToSODTO(SalesOrder so) {
        List<SalesOrderLineDTO> lineDTOs = so.getLines().stream().map(l -> SalesOrderLineDTO.builder()
                .id(l.getId())
                .productId(l.getProduct().getId())
                .productName(l.getProduct().getName())
                .productCode(l.getProduct().getCode())
                .quantity(l.getQuantity())
                .unitPrice(l.getUnitPrice())
                .taxRate(l.getTaxRate())
                .subtotal(l.getSubtotal())
                .build()
        ).collect(Collectors.toList());

        return SalesOrderDTO.builder()
                .id(so.getId())
                .soNumber(so.getSoNumber())
                .customerId(so.getCustomer().getId())
                .customerName(so.getCustomer().getName())
                .orderDate(so.getOrderDate())
                .status(so.getStatus())
                .totalAmount(so.getTotalAmount())
                .lines(lineDTOs)
                .build();
    }

    public CustomerInvoiceDTO mapToInvoiceDTO(CustomerInvoice inv) {
        return CustomerInvoiceDTO.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .soId(inv.getSalesOrder() != null ? inv.getSalesOrder().getId() : null)
                .soNumber(inv.getSalesOrder() != null ? inv.getSalesOrder().getSoNumber() : null)
                .customerId(inv.getCustomer().getId())
                .customerName(inv.getCustomer().getName())
                .invoiceDate(inv.getInvoiceDate())
                .dueDate(inv.getDueDate())
                .totalAmount(inv.getTotalAmount())
                .status(inv.getStatus())
                .journalEntryId(inv.getJournalEntry() != null ? inv.getJournalEntry().getId() : null)
                .build();
    }
}
