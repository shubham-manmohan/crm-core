/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.quotation.controller;
import com.mini2more.crm.modules.quotation.entity.Vendor;
import com.mini2more.crm.modules.quotation.entity.RfqVendor;
import com.mini2more.crm.modules.quotation.repository.RfqRepository;
import com.mini2more.crm.modules.quotation.repository.RfqVendorRepository;
import com.mini2more.crm.modules.quotation.entity.Rfq;
import com.mini2more.crm.modules.quotation.repository.VendorRepository;

import com.mini2more.crm.common.dto.ApiResponse;
import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.RfqStatus;
import com.mini2more.crm.common.exception.ResourceNotFoundException;
import com.mini2more.crm.config.AuditService;
import com.mini2more.crm.modules.crm.entity.LeadEntity;
import com.mini2more.crm.modules.crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rfqs")
@RequiredArgsConstructor
public class RfqController {

    private final RfqRepository rfqRepository;
    private final RfqVendorRepository rfqVendorRepository;
    private final VendorRepository vendorRepository;
    private final LeadRepository leadRepository;
    private final AuditService auditService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER')")
    public ResponseEntity<ApiResponse<Rfq>> createRfq(@RequestBody Rfq rfq) {
        // Generate RFQ number
        Long maxId = rfqRepository.findMaxId();
        long nextId = (maxId != null ? maxId : 0) + 1;
        rfq.setRfqNumber(String.format("RFQ-%06d", nextId));
        rfq.setStatus(RfqStatus.CREATED);

        if (rfq.getLead() != null && rfq.getLead().getId() != null) {
            LeadEntity lead = leadRepository.findById(rfq.getLead().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lead", rfq.getLead().getId()));
            rfq.setLead(lead);
        }

        Rfq saved = rfqRepository.save(rfq);
        auditService.log("CREATE", "RFQ", saved.getId(), "RFQ created: " + saved.getRfqNumber());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("RFQ created", saved));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER', 'SALES')")
    public ResponseEntity<ApiResponse<PageResponse<Rfq>>> getRfqs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) RfqStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Rfq> rfqPage = rfqRepository.searchRfqs(search, status, pageable);

        PageResponse<Rfq> response = PageResponse.<Rfq>builder()
                .content(rfqPage.getContent())
                .page(rfqPage.getNumber())
                .size(rfqPage.getSize())
                .totalElements(rfqPage.getTotalElements())
                .totalPages(rfqPage.getTotalPages())
                .last(rfqPage.isLast())
                .first(rfqPage.isFirst())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER', 'SALES')")
    public ResponseEntity<ApiResponse<Rfq>> getRfqById(@PathVariable Long id) {
        Rfq rfq = rfqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", id));
        return ResponseEntity.ok(ApiResponse.success(rfq));
    }

    /**
     * Add a vendor to an RFQ for price comparison
     */
    @PostMapping("/{rfqId}/vendors/{vendorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER')")
    public ResponseEntity<ApiResponse<RfqVendor>> addVendorToRfq(
            @PathVariable Long rfqId, @PathVariable Long vendorId) {
        Rfq rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", rfqId));
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));

        RfqVendor rfqVendor = RfqVendor.builder()
                .rfq(rfq)
                .vendor(vendor)
                .sentAt(LocalDateTime.now())
                .isSelected(false)
                .build();

        RfqVendor saved = rfqVendorRepository.save(rfqVendor);

        // Update RFQ status
        rfq.setStatus(RfqStatus.SENT_TO_VENDOR);
        rfqRepository.save(rfq);

        return ResponseEntity.ok(ApiResponse.success("Vendor added to RFQ", saved));
    }

    /**
     * Record vendor's price response
     */
    @PutMapping("/vendor-response/{rfqVendorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER')")
    public ResponseEntity<ApiResponse<RfqVendor>> recordVendorResponse(
            @PathVariable Long rfqVendorId, @RequestBody RfqVendor response) {
        RfqVendor existing = rfqVendorRepository.findById(rfqVendorId)
                .orElseThrow(() -> new ResourceNotFoundException("RfqVendor", rfqVendorId));

        existing.setUnitPrice(response.getUnitPrice());
        existing.setTotalPrice(response.getTotalPrice());
        existing.setQuantity(response.getQuantity());
        existing.setDeliveryDays(response.getDeliveryDays());
        existing.setPaymentTerms(response.getPaymentTerms());
        existing.setWarrantyMonths(response.getWarrantyMonths());
        existing.setRemarks(response.getRemarks());
        existing.setRespondedAt(LocalDateTime.now());

        RfqVendor saved = rfqVendorRepository.save(existing);

        // Update RFQ status
        Rfq rfq = existing.getRfq();
        rfq.setStatus(RfqStatus.RESPONSE_RECEIVED);
        rfqRepository.save(rfq);

        return ResponseEntity.ok(ApiResponse.success("Vendor response recorded", saved));
    }

    /**
     * Select a vendor for the RFQ (best price/terms)
     */
    @PatchMapping("/vendor-response/{rfqVendorId}/select")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER')")
    public ResponseEntity<ApiResponse<RfqVendor>> selectVendor(
            @PathVariable Long rfqVendorId) {
        RfqVendor existing = rfqVendorRepository.findById(rfqVendorId)
                .orElseThrow(() -> new ResourceNotFoundException("RfqVendor", rfqVendorId));

        existing.setIsSelected(true);
        RfqVendor saved = rfqVendorRepository.save(existing);

        return ResponseEntity.ok(ApiResponse.success("Vendor selected", saved));
    }
}
