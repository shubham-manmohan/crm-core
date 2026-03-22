/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.quotation.controller;
import com.mini2more.crm.modules.quotation.entity.Quotation;
import com.mini2more.crm.modules.quotation.service.QuotationService;

import com.mini2more.crm.common.dto.ApiResponse;
import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.QuotationStatus;
import com.mini2more.crm.modules.quotation.dto.CreateQuotationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<Quotation>> createQuotation(
            @Valid @RequestBody CreateQuotationRequest request) {
        Quotation created = quotationService.createQuotation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quotation created", created));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER')")
    public ResponseEntity<ApiResponse<PageResponse<Quotation>>> getQuotations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) QuotationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<Quotation> response = quotationService.getQuotations(
                search, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER')")
    public ResponseEntity<ApiResponse<Quotation>> getQuotationById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(quotationService.getQuotationById(id)));
    }

    @GetMapping("/lead/{leadId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER')")
    public ResponseEntity<ApiResponse<List<Quotation>>> getQuotationsByLead(
            @PathVariable Long leadId) {
        return ResponseEntity.ok(ApiResponse.success(quotationService.getQuotationsByLead(leadId)));
    }

    @PostMapping("/{id}/revision")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<Quotation>> createRevision(
            @PathVariable Long id,
            @Valid @RequestBody CreateQuotationRequest request) {
        Quotation revision = quotationService.createRevision(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Revision created", revision));
    }

    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<Quotation>> submitForApproval(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Submitted for approval",
                quotationService.submitForApproval(id)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Quotation>> approveQuotation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Quotation approved",
                quotationService.approveQuotation(id)));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Quotation>> rejectQuotation(
            @PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success("Quotation rejected",
                quotationService.rejectQuotation(id, reason)));
    }

    @PatchMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<Quotation>> markAsSent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Quotation sent to client",
                quotationService.markAsSent(id)));
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<Quotation>> markAccepted(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Quotation accepted",
                quotationService.markAccepted(id)));
    }
}
