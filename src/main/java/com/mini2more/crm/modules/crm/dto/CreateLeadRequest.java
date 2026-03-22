/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.crm.dto;

import com.mini2more.crm.common.enums.LeadPriority;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateLeadRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    // Client info (optional if client_id is provided)
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String clientCompany;

    // Assignment
    private Long assignedToId;
    private Long branchId;

    // Priority
    private LeadPriority priority;

    // Source & Category
    private String source;
    private String industry;
    private String application;

    // Value
    private BigDecimal estimatedValue;

    // Products
    private String productRequirements;
    private String quantity;
    private String brandPreference;

    // Follow-up
    private LocalDateTime nextFollowUp;
    private String followUpNotes;

    // Project
    private String projectName;
    private String tags;
}
