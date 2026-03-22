/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadDashboardStats {
    private long totalLeads;
    private long newLeads;
    private long assignedLeads;
    private long inDiscussion;
    private long quotationSent;
    private long negotiation;
    private long orderConfirmed;
    private long lost;
    private long onHold;
    private long slaBreached;
    private long followUpsDueToday;
    private Map<String, Long> leadsBySource;
    private Map<String, Long> leadsByPriority;
}
