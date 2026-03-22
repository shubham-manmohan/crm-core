/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.crm.controller;
import com.mini2more.crm.modules.crm.service.LeadService;

import com.mini2more.crm.common.dto.ApiResponse;
import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.LeadPriority;
import com.mini2more.crm.common.enums.LeadStatus;
import com.mini2more.crm.modules.crm.dto.AddActivityRequest;
import com.mini2more.crm.modules.crm.dto.CreateLeadRequest;
import com.mini2more.crm.modules.crm.dto.LeadActivityResponse;
import com.mini2more.crm.modules.crm.dto.LeadDashboardStats;
import com.mini2more.crm.modules.crm.dto.LeadResponse;
import com.mini2more.crm.modules.crm.dto.UpdateLeadRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    /**
     * Create a new lead (Admin, Sales, Purchaser)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER')")
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(
            @Valid @RequestBody CreateLeadRequest request) {
        LeadResponse response = leadService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lead created successfully", response));
    }

    /**
     * Get all leads with pagination, search, sort, and filters
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER')")
    public ResponseEntity<ApiResponse<PageResponse<LeadResponse>>> getAllLeads(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) LeadPriority priority,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<LeadResponse> response = leadService.getAllLeads(
                search, status, priority, assignedToId, branchId, source,
                page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get lead by ID (includes activities)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER')")
    public ResponseEntity<ApiResponse<LeadResponse>> getLeadById(@PathVariable Long id) {
        LeadResponse response = leadService.getLeadById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get lead by lead number
     */
    @GetMapping("/number/{leadNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER')")
    public ResponseEntity<ApiResponse<LeadResponse>> getLeadByNumber(
            @PathVariable String leadNumber) {
        LeadResponse response = leadService.getLeadByNumber(leadNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update lead
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER')")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLead(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeadRequest request) {
        LeadResponse response = leadService.updateLead(id, request);
        return ResponseEntity.ok(ApiResponse.success("Lead updated", response));
    }

    /**
     * Change lead status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER')")
    public ResponseEntity<ApiResponse<LeadResponse>> changeStatus(
            @PathVariable Long id,
            @RequestParam LeadStatus status) {
        LeadResponse response = leadService.changeStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Status updated", response));
    }

    /**
     * Assign lead to a user
     */
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<LeadResponse>> assignLead(
            @PathVariable Long id,
            @RequestParam Long assigneeId) {
        LeadResponse response = leadService.assignLead(id, assigneeId);
        return ResponseEntity.ok(ApiResponse.success("Lead assigned", response));
    }

    /**
     * Add activity to a lead (note, call, email, meeting, follow-up)
     */
    @PostMapping("/{id}/activities")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER')")
    public ResponseEntity<ApiResponse<LeadActivityResponse>> addActivity(
            @PathVariable Long id,
            @Valid @RequestBody AddActivityRequest request) {
        LeadActivityResponse response = leadService.addActivity(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Activity added", response));
    }

    /**
     * Get all activities for a lead
     */
    @GetMapping("/{id}/activities")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER')")
    public ResponseEntity<ApiResponse<List<LeadActivityResponse>>> getActivities(
            @PathVariable Long id) {
        List<LeadActivityResponse> activities = leadService.getActivities(id);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }

    /**
     * Dashboard stats
     */
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER')")
    public ResponseEntity<ApiResponse<LeadDashboardStats>> getDashboardStats() {
        LeadDashboardStats stats = leadService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
