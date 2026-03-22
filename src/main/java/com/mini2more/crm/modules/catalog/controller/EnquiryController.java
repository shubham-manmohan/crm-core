/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.catalog.controller;
import com.mini2more.crm.modules.catalog.service.CatalogService;
import com.mini2more.crm.modules.catalog.entity.Enquiry;

import com.mini2more.crm.common.dto.ApiResponse;
import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.EnquiryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin-facing enquiry management controller.
 */
@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
public class EnquiryController {

    private final CatalogService catalogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<PageResponse<Enquiry>>> getEnquiries(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EnquiryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(
                catalogService.getEnquiries(search, status, page, size, sortBy, sortDir)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<Enquiry>> getEnquiryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getEnquiryById(id)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<Enquiry>> updateStatus(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        EnquiryStatus status = EnquiryStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                catalogService.updateEnquiryStatus(id, status)));
    }
}
