/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.finance.controller;
import com.mini2more.crm.modules.finance.entity.CreditDebitNote;
import com.mini2more.crm.modules.finance.service.InvoiceService;
import com.mini2more.crm.modules.finance.entity.Payment;
import com.mini2more.crm.modules.finance.entity.Invoice;

import com.mini2more.crm.common.dto.ApiResponse;
import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.InvoiceStatus;
import com.mini2more.crm.common.enums.PaymentMode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<Invoice>> createInvoice(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice created", invoiceService.createInvoice(payload)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<ApiResponse<PageResponse<Invoice>>> getInvoices(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(
                invoiceService.getInvoices(search, status, page, size, sortBy, sortDir)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<ApiResponse<Invoice>> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getById(id)));
    }

    @PatchMapping("/{id}/issue")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<Invoice>> issueInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Invoice issued", invoiceService.issueInvoice(id)));
    }

    @PostMapping("/{id}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<Payment>> recordPayment(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        PaymentMode mode = PaymentMode.valueOf(body.getOrDefault("paymentMode", "NEFT").toString());
        String ref = body.getOrDefault("referenceNumber", "").toString();
        String bank = body.getOrDefault("bankName", "").toString();
        String notes = body.getOrDefault("notes", "").toString();
        return ResponseEntity.ok(ApiResponse.success("Payment recorded",
                invoiceService.recordPayment(id, amount, mode, ref, bank, notes)));
    }

    @GetMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<Payment>>> getPayments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getPaymentsForInvoice(id)));
    }

    @PostMapping("/{id}/note")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<CreditDebitNote>> createNote(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        CreditDebitNote.NoteType type = CreditDebitNote.NoteType.valueOf(
                body.getOrDefault("noteType", "CREDIT").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String reason = body.getOrDefault("reason", "").toString();
        String notes = body.getOrDefault("notes", "").toString();
        return ResponseEntity.ok(ApiResponse.success("Note created",
                invoiceService.createNote(id, type, amount, reason, notes)));
    }

    @GetMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<CreditDebitNote>>> getNotes(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getNotesForInvoice(id)));
    }

    @GetMapping("/outstanding")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<ApiResponse<BigDecimal>> getOutstanding(@RequestParam String company) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getOutstandingForCompany(company)));
    }
}
