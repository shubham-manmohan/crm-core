/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.crm.dto;

import com.mini2more.crm.common.enums.LeadPriority;
import com.mini2more.crm.common.enums.LeadStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateLeadRequest {
    private String title;
    private String description;
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String clientCompany;
    private Long assignedToId;
    private Long branchId;
    private LeadStatus status;
    private LeadPriority priority;
    private String source;
    private String industry;
    private String application;
    private BigDecimal estimatedValue;
    private LocalDateTime nextFollowUp;
    private String followUpNotes;
    private String productRequirements;
    private String quantity;
    private String brandPreference;
    private String lostReason;
    private String projectName;
    private String tags;
}
