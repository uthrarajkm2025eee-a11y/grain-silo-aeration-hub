package com.agri.silo.service;

import com.agri.silo.dto.CreateJournalEntryRequest;
import com.agri.silo.dto.CreatePaymentRequest;
import com.agri.silo.dto.JournalEntryDTO;
import com.agri.silo.dto.PaymentDTO;
import com.agri.silo.entity.*;
import com.agri.silo.enums.BillStatus;
import com.agri.silo.enums.InvoiceStatus;
import com.agri.silo.enums.OrderStatus;
import com.agri.silo.enums.PaymentType;
import com.agri.silo.exception.ResourceNotFoundException;
import com.agri.silo.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ContactRepository contactRepository;
    private final AccountRepository accountRepository;
    private final VendorBillRepository billRepository;
    private final CustomerInvoiceRepository invoiceRepository;
    private final PurchaseOrderRepository poRepository;
    private final SalesOrderRepository soRepository;
    private final JournalRepository journalRepository;
    private final AccountingService accountingService;

    public PaymentService(PaymentRepository paymentRepository,
                          ContactRepository contactRepository,
                          AccountRepository accountRepository,
                          VendorBillRepository billRepository,
                          CustomerInvoiceRepository invoiceRepository,
                          PurchaseOrderRepository poRepository,
                          SalesOrderRepository soRepository,
                          JournalRepository journalRepository,
                          AccountingService accountingService) {
        this.paymentRepository = paymentRepository;
        this.contactRepository = contactRepository;
        this.accountRepository = accountRepository;
        this.billRepository = billRepository;
        this.invoiceRepository = invoiceRepository;
        this.poRepository = poRepository;
        this.soRepository = soRepository;
        this.journalRepository = journalRepository;
        this.accountingService = accountingService;
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getPaymentsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentRepository.findAllPaged(pageable).map(this::mapToPaymentDTO);
    }

    @Transactional
    public PaymentDTO createPayment(CreatePaymentRequest request) {
        Contact partner = contactRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner contact not found"));

        Account bankAcc = accountRepository.findById(request.getBankAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));

        VendorBill bill = null;
        if (request.getBillId() != null) {
            bill = billRepository.findById(request.getBillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + request.getBillId()));
        }

        CustomerInvoice invoice = null;
        if (request.getInvoiceId() != null) {
            invoice = invoiceRepository.findById(request.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + request.getInvoiceId()));
        }

        String payNum = "PAY/" + LocalDate.now().getYear() + "/" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Journal bankJournal = journalRepository.findByCode("BNK")
                .orElseGet(() -> journalRepository.findAll().get(0));

        CreateJournalEntryRequest jeReq = new CreateJournalEntryRequest();
        jeReq.setJournalId(bankJournal.getId());
        jeReq.setEntryDate(request.getPaymentDate());
        jeReq.setReference("Payment " + payNum + " for " + partner.getName());

        List<CreateJournalEntryRequest.ItemRequest> items = new ArrayList<>();

        if (request.getPaymentType() == PaymentType.OUTBOUND) {
            // Debit Vendor Payables (2010), Credit Bank (1030)
            Account payableAcc = accountRepository.findByCode("2010")
                    .orElseThrow(() -> new ResourceNotFoundException("Payable account 2010 not found"));

            CreateJournalEntryRequest.ItemRequest debitItem = new CreateJournalEntryRequest.ItemRequest();
            debitItem.setAccountId(payableAcc.getId());
            debitItem.setLabel("Payment to Vendor - " + partner.getName());
            debitItem.setDebit(request.getAmount());
            debitItem.setCredit(BigDecimal.ZERO);
            items.add(debitItem);

            CreateJournalEntryRequest.ItemRequest creditItem = new CreateJournalEntryRequest.ItemRequest();
            creditItem.setAccountId(bankAcc.getId());
            creditItem.setLabel("Bank Disbursement");
            creditItem.setDebit(BigDecimal.ZERO);
            creditItem.setCredit(request.getAmount());
            items.add(creditItem);

            if (bill != null) {
                bill.setStatus(BillStatus.PAID);
                if (bill.getPurchaseOrder() != null) {
                    PurchaseOrder po = bill.getPurchaseOrder();
                    po.setStatus(OrderStatus.PAID);
                    poRepository.save(po);
                }
                billRepository.save(bill);
            }
        } else {
            // INBOUND Payment
            // Debit Bank (1030), Credit Accounts Receivable (1040)
            Account recAcc = accountRepository.findByCode("1040")
                    .orElseThrow(() -> new ResourceNotFoundException("Receivable account 1040 not found"));

            CreateJournalEntryRequest.ItemRequest debitItem = new CreateJournalEntryRequest.ItemRequest();
            debitItem.setAccountId(bankAcc.getId());
            debitItem.setLabel("Bank Deposit - Customer Payment");
            debitItem.setDebit(request.getAmount());
            debitItem.setCredit(BigDecimal.ZERO);
            items.add(debitItem);

            CreateJournalEntryRequest.ItemRequest creditItem = new CreateJournalEntryRequest.ItemRequest();
            creditItem.setAccountId(recAcc.getId());
            creditItem.setLabel("Customer Receipt - " + partner.getName());
            creditItem.setDebit(BigDecimal.ZERO);
            creditItem.setCredit(request.getAmount());
            items.add(creditItem);

            if (invoice != null) {
                invoice.setStatus(InvoiceStatus.PAID);
                if (invoice.getSalesOrder() != null) {
                    SalesOrder so = invoice.getSalesOrder();
                    so.setStatus(OrderStatus.PAID);
                    soRepository.save(so);
                }
                invoiceRepository.save(invoice);
            }
        }

        jeReq.setItems(items);
        JournalEntryDTO jeDTO = accountingService.createJournalEntry(jeReq);

        Payment payment = Payment.builder()
                .paymentNumber(payNum)
                .paymentDate(request.getPaymentDate())
                .amount(request.getAmount())
                .paymentType(request.getPaymentType())
                .partner(partner)
                .bankAccount(bankAcc)
                .bill(bill)
                .invoice(invoice)
                .journalEntry(JournalEntry.builder().id(jeDTO.getId()).build())
                .build();

        Payment saved = paymentRepository.save(payment);
        return mapToPaymentDTO(saved);
    }

    private PaymentDTO mapToPaymentDTO(Payment p) {
        return PaymentDTO.builder()
                .id(p.getId())
                .paymentNumber(p.getPaymentNumber())
                .paymentDate(p.getPaymentDate())
                .amount(p.getAmount())
                .paymentType(p.getPaymentType())
                .partnerId(p.getPartner().getId())
                .partnerName(p.getPartner().getName())
                .bankAccountId(p.getBankAccount().getId())
                .bankAccountName(p.getBankAccount().getName())
                .billId(p.getBill() != null ? p.getBill().getId() : null)
                .billNumber(p.getBill() != null ? p.getBill().getBillNumber() : null)
                .invoiceId(p.getInvoice() != null ? p.getInvoice().getId() : null)
                .invoiceNumber(p.getInvoice() != null ? p.getInvoice().getInvoiceNumber() : null)
                .journalEntryId(p.getJournalEntry() != null ? p.getJournalEntry().getId() : null)
                .build();
    }
}
