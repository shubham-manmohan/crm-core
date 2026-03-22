/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.quotation.controller;
import com.mini2more.crm.modules.quotation.entity.Vendor;
import com.mini2more.crm.modules.quotation.service.VendorService;

import com.mini2more.crm.common.dto.ApiResponse;
import com.mini2more.crm.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER', 'SALES')")
    public ResponseEntity<ApiResponse<PageResponse<Vendor>>> getVendors(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<Vendor> response = vendorService.getVendors(search, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER', 'SALES')")
    public ResponseEntity<ApiResponse<Vendor>> getVendorById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getVendorById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER')")
    public ResponseEntity<ApiResponse<Vendor>> createVendor(@Valid @RequestBody Vendor vendor) {
        Vendor created = vendorService.createVendor(vendor);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor created", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER')")
    public ResponseEntity<ApiResponse<Vendor>> updateVendor(
            @PathVariable Long id, @Valid @RequestBody Vendor vendor) {
        Vendor updated = vendorService.updateVendor(id, vendor);
        return ResponseEntity.ok(ApiResponse.success("Vendor updated", updated));
    }
}
