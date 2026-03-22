/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiDashboardResponse {
    
    // Top-level KPIs
    private BigDecimal totalSales;         // Sum of total_amount for all non-cancelled orders
    private BigDecimal totalOutstanding;   // Sum of balance_due for all non-paid invoices
    private Long activeLeads;              // Count of OPEN/IN_PROGRESS leads
    private BigDecimal averageMargin;      // Avg margin percent from all won quotes/orders

    // Line Chart: Sales over the last 6-12 months
    private List<SalesDataPoint> monthlySales;

    // Bar/Pie Chart: Sales aggregated by Brand
    private List<ChartDataPoint> salesByBrand;

    // SLA Compliance (Pie Chart)
    private Long slaCompliantLeads;
    private Long slaBreachedLeads;
}
