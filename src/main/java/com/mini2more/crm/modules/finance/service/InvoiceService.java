/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.finance.service;
import com.mini2more.crm.modules.finance.entity.CreditDebitNote;
import com.mini2more.crm.modules.finance.repository.PaymentRepository;
import com.mini2more.crm.modules.finance.entity.Payment;
import com.mini2more.crm.modules.finance.entity.Invoice;
import com.mini2more.crm.modules.finance.entity.InvoiceItem;
import com.mini2more.crm.modules.finance.repository.CreditDebitNoteRepository;
import com.mini2more.crm.modules.finance.repository.InvoiceRepository;

import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.InvoiceStatus;
import com.mini2more.crm.common.enums.PaymentMode;
import com.mini2more.crm.common.exception.BusinessException;
import com.mini2more.crm.common.exception.ResourceNotFoundException;
import com.mini2more.crm.config.AuditService;
import com.mini2more.crm.modules.orders.entity.SalesOrder;
import com.mini2more.crm.modules.orders.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final CreditDebitNoteRepository noteRepository;
    private final SalesOrderRepository orderRepository;
    private final AuditService auditService;

    // ─── CREATE INVOICE ───────────────────────────────────────────

    @Transactional
    @SuppressWarnings("unchecked")
    public Invoice createInvoice(Map<String, Object> payload) {
        Long maxId = invoiceRepository.findMaxId();
        long nextId = (maxId != null ? maxId : 0) + 1;

        boolean isIgst = Boolean.TRUE.equals(payload.get("isIgst"));

        Invoice.InvoiceBuilder builder = Invoice.builder()
                .invoiceNumber(String.format("INV-%06d", nextId))
                .status(InvoiceStatus.DRAFT)
                .clientName(str(payload, "clientName"))
                .clientCompany(str(payload, "clientCompany"))
                .clientGst(str(payload, "clientGst"))
                .clientAddress(str(payload, "clientAddress"))
                .clientState(str(payload, "clientState"))
                .invoiceDate(LocalDate.now())
                .isIgst(isIgst)
                .notes(str(payload, "notes"))
                .termsConditions(str(payload, "termsConditions"))
                .items(new ArrayList<>());

        if (payload.get("dueDate") != null) {
            builder.dueDate(LocalDate.parse(payload.get("dueDate").toString()));
        } else {
            builder.dueDate(LocalDate.now().plusDays(30));
        }

        // Link to order
        if (payload.get("orderId") != null) {
            Long orderId = Long.valueOf(payload.get("orderId").toString());
            SalesOrder order = orderRepository.findById(orderId).orElse(null);
            builder.salesOrder(order);
            if (order != null && order.getBranch() != null) {
                builder.branch(order.getBranch());
            }
        }

        Invoice invoice = builder.build();

        // Process items with GST calculation
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) payload.get("items");
        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO, totalSgst = BigDecimal.ZERO, totalIgst = BigDecimal.ZERO;

        if (itemsData != null) {
            for (Map<String, Object> itemData : itemsData) {
                int qty = Integer.parseInt(itemData.getOrDefault("quantity", "1").toString());
                BigDecimal unitPrice = new BigDecimal(itemData.getOrDefault("unitPrice", "0").toString());
                BigDecimal gstPct = new BigDecimal(itemData.getOrDefault("gstPercent", "18").toString());

                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
                BigDecimal gstAmt = lineTotal.multiply(gstPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                InvoiceItem.InvoiceItemBuilder ib = InvoiceItem.builder()
                        .invoice(invoice)
                        .productName(itemData.getOrDefault("productName", "").toString())
                        .productCode(str(itemData, "productCode"))
                        .hsnCode(str(itemData, "hsnCode"))
                        .unit(itemData.getOrDefault("unit", "Nos").toString())
                        .quantity(qty)
                        .unitPrice(unitPrice)
                        .gstPercent(gstPct)
                        .taxableAmount(lineTotal);

                if (isIgst) {
                    ib.igstAmount(gstAmt).cgstAmount(BigDecimal.ZERO).sgstAmount(BigDecimal.ZERO);
                    totalIgst = totalIgst.add(gstAmt);
                } else {
                    BigDecimal half = gstAmt.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                    ib.cgstAmount(half).sgstAmount(half).igstAmount(BigDecimal.ZERO);
                    totalCgst = totalCgst.add(half);
                    totalSgst = totalSgst.add(half);
                }
                ib.totalAmount(lineTotal.add(gstAmt));
                totalTaxable = totalTaxable.add(lineTotal);

                invoice.getItems().add(ib.build());
            }
        }

        BigDecimal discount = payload.get("discountAmount") != null
                ? new BigDecimal(payload.get("discountAmount").toString()) : BigDecimal.ZERO;
        BigDecimal totalGst = totalCgst.add(totalSgst).add(totalIgst);
        BigDecimal grandTotal = totalTaxable.subtract(discount).add(totalGst);

        invoice.setTaxableAmount(totalTaxable);
        invoice.setCgstAmount(totalCgst);
        invoice.setSgstAmount(totalSgst);
        invoice.setIgstAmount(totalIgst);
        invoice.setDiscountAmount(discount);
        invoice.setTotalAmount(grandTotal);
        invoice.setBalanceDue(grandTotal);
        invoice.setPaidAmount(BigDecimal.ZERO);

        Invoice saved = invoiceRepository.save(invoice);
        auditService.log("CREATE", "INVOICE", saved.getId(), "Invoice created: " + saved.getInvoiceNumber());
        return saved;
    }

    // ─── ISSUE INVOICE ────────────────────────────────────────────

    @Transactional
    public Invoice issueInvoice(Long id) {
        Invoice inv = getById(id);
        if (inv.getStatus() != InvoiceStatus.DRAFT) throw new BusinessException("Only DRAFT invoices can be issued");

        // Credit limit check
        if (inv.getClientCompany() != null) {
            BigDecimal outstanding = invoiceRepository.getOutstandingByCompany(inv.getClientCompany());
            BigDecimal creditLimit = new BigDecimal("500000"); // Default 5 lakh limit
            if (outstanding.add(inv.getTotalAmount()).compareTo(creditLimit) > 0) {
                log.warn("Credit limit warning for {}: outstanding ₹{} + new ₹{} > limit ₹{}",
                        inv.getClientCompany(), outstanding, inv.getTotalAmount(), creditLimit);
            }
        }

        inv.setStatus(InvoiceStatus.ISSUED);
        auditService.log("ISSUE", "INVOICE", inv.getId(), "Invoice issued");
        return invoiceRepository.save(inv);
    }

    // ─── RECORD PAYMENT ───────────────────────────────────────────

    @Transactional
    public Payment recordPayment(Long invoiceId, BigDecimal amount, PaymentMode mode,
                                  String referenceNumber, String bankName, String notes) {
        Invoice inv = getById(invoiceId);
        if (inv.getStatus() == InvoiceStatus.CANCELLED || inv.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Cannot record payment on " + inv.getStatus() + " invoice");
        }
        if (amount.compareTo(inv.getBalanceDue()) > 0) {
            throw new BusinessException("Payment ₹" + amount + " exceeds balance ₹" + inv.getBalanceDue());
        }

        Long maxPmtId = paymentRepository.findMaxId();
        long nextPmtId = (maxPmtId != null ? maxPmtId : 0) + 1;

        Payment payment = Payment.builder()
                .paymentNumber(String.format("PMT-%06d", nextPmtId))
                .invoice(inv)
                .amount(amount)
                .paymentMode(mode)
                .paymentDate(LocalDate.now())
                .referenceNumber(referenceNumber)
                .bankName(bankName)
                .notes(notes)
                .build();

        Payment saved = paymentRepository.save(payment);

        inv.setPaidAmount(inv.getPaidAmount().add(amount));
        inv.setBalanceDue(inv.getTotalAmount().subtract(inv.getPaidAmount()));

        if (inv.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) {
            inv.setStatus(InvoiceStatus.PAID);
        } else {
            inv.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        invoiceRepository.save(inv);

        auditService.log("PAYMENT", "INVOICE", invoiceId,
                "Payment ₹" + amount + " via " + mode + " (Ref: " + referenceNumber + ")");
        return saved;
    }

    // ─── CREDIT/DEBIT NOTE ────────────────────────────────────────

    @Transactional
    public CreditDebitNote createNote(Long invoiceId, CreditDebitNote.NoteType type,
                                       BigDecimal amount, String reason, String notes) {
        Invoice inv = getById(invoiceId);

        Long maxNoteId = noteRepository.findMaxId();
        long nextNoteId = (maxNoteId != null ? maxNoteId : 0) + 1;

        String prefix = type == CreditDebitNote.NoteType.CREDIT ? "CN" : "DN";

        CreditDebitNote note = CreditDebitNote.builder()
                .noteNumber(String.format("%s-%06d", prefix, nextNoteId))
                .noteType(type)
                .invoice(inv)
                .amount(amount)
                .reason(reason)
                .noteDate(LocalDate.now())
                .notes(notes)
                .build();

        CreditDebitNote saved = noteRepository.save(note);

        // Credit note reduces balance due
        if (type == CreditDebitNote.NoteType.CREDIT) {
            inv.setBalanceDue(inv.getBalanceDue().subtract(amount));
            if (inv.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) {
                inv.setStatus(InvoiceStatus.CREDIT_NOTE_ISSUED);
            }
            invoiceRepository.save(inv);
        }

        auditService.log("NOTE", "INVOICE", invoiceId,
                type + " note " + saved.getNoteNumber() + " for ₹" + amount);
        return saved;
    }

    // ─── READ ─────────────────────────────────────────────────────

    public Invoice getById(Long id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    public PageResponse<Invoice> getInvoices(String search, InvoiceStatus status,
                                              int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<Invoice> p = invoiceRepository.search(search, status, PageRequest.of(page, size, sort));
        return PageResponse.<Invoice>builder()
                .content(p.getContent()).page(p.getNumber()).size(p.getSize())
                .totalElements(p.getTotalElements()).totalPages(p.getTotalPages())
                .last(p.isLast()).first(p.isFirst()).build();
    }

    public List<Payment> getPaymentsForInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }

    public List<CreditDebitNote> getNotesForInvoice(Long invoiceId) {
        return noteRepository.findByInvoiceId(invoiceId);
    }

    public BigDecimal getOutstandingForCompany(String company) {
        return invoiceRepository.getOutstandingByCompany(company);
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
