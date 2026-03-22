/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.crm.dto;

import com.mini2more.crm.common.enums.LeadPriority;
import com.mini2more.crm.common.enums.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponse {
    private Long id;
    private String leadNumber;
    private String title;
    private String description;

    // Client
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String clientCompany;

    // Assignment
    private Long assignedToId;
    private String assignedToName;
    private Long branchId;
    private String branchName;

    // Status
    private LeadStatus status;
    private LeadPriority priority;

    // Source
    private String source;
    private String industry;
    private String application;

    // Value
    private BigDecimal estimatedValue;

    // SLA
    private LocalDateTime slaDeadline;
    private Boolean slaBreached;
    private Integer escalationLevel;

    // Follow-up
    private LocalDateTime nextFollowUp;
    private String followUpNotes;

    // Products
    private String productRequirements;
    private String quantity;
    private String brandPreference;

    // Lost
    private String lostReason;

    // Project
    private String projectName;
    private String tags;
    private String attachmentPaths;

    // Activities (loaded on detail view only)
    private List<LeadActivityResponse> activities;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
}
