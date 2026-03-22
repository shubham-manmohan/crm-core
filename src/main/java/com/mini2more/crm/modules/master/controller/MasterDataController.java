/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.master.controller;
import com.mini2more.crm.modules.master.service.MasterDataService;
import com.mini2more.crm.modules.master.dto.MasterDataRequest;
import com.mini2more.crm.modules.master.dto.MasterDataResponse;

import com.mini2more.crm.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
public class MasterDataController {

    private final MasterDataService masterDataService;

    /**
     * Get all active master data by type (public endpoint for dropdowns)
     * Types: STATE, CITY, INDUSTRY, APPLICATION, UNIT, GST_CODE, PAYMENT_TERM,
     * LEAD_SOURCE, PRODUCT_CATEGORY, BRAND
     */
    @GetMapping("/{type}")
    public ResponseEntity<ApiResponse<List<MasterDataResponse>>> getByType(@PathVariable String type) {
        List<MasterDataResponse> data = masterDataService.getByType(type);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Get master data by type and parent (e.g., cities for a given state)
     */
    @GetMapping("/{type}/parent/{parentCode}")
    public ResponseEntity<ApiResponse<List<MasterDataResponse>>> getByTypeAndParent(
            @PathVariable String type,
            @PathVariable String parentCode) {
        List<MasterDataResponse> data = masterDataService.getByTypeAndParent(type, parentCode);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Search master data by type with keyword
     */
    @GetMapping("/{type}/search")
    public ResponseEntity<ApiResponse<List<MasterDataResponse>>> search(
            @PathVariable String type,
            @RequestParam(required = false) String q) {
        List<MasterDataResponse> data = masterDataService.search(type, q);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Create new master data (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MasterDataResponse>> create(@Valid @RequestBody MasterDataRequest request) {
        MasterDataResponse response = masterDataService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Master data created", response));
    }

    /**
     * Update master data (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MasterDataResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody MasterDataRequest request) {
        MasterDataResponse response = masterDataService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Master data updated", response));
    }

    /**
     * Soft-delete master data (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        masterDataService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Master data deactivated", null));
    }
}
