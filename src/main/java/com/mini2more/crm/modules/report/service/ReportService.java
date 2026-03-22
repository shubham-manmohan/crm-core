/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.report.service;

import com.mini2more.crm.modules.finance.repository.InvoiceRepository;
import com.mini2more.crm.modules.crm.repository.LeadRepository;
import com.mini2more.crm.modules.orders.repository.SalesOrderRepository;
import com.mini2more.crm.modules.quotation.repository.QuotationRepository;
import com.mini2more.crm.modules.report.dto.ChartDataPoint;
import com.mini2more.crm.modules.report.dto.KpiDashboardResponse;
import com.mini2more.crm.modules.report.dto.SalesDataPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final SalesOrderRepository salesOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final LeadRepository leadRepository;
    private final QuotationRepository quotationRepository;

    @Transactional(readOnly = true)
    public KpiDashboardResponse getDashboardKpis() {
        log.info("Fetching KPI Dashboard metrics");

        BigDecimal totalSales = salesOrderRepository.sumTotalSales();
        BigDecimal totalOutstanding = invoiceRepository.sumTotalOutstanding();
        long activeLeads = leadRepository.countActiveLeads();
        BigDecimal avgMargin = quotationRepository.findAverageMargin();

        long slaBreached = leadRepository.countSlaBreachedLeads();
        long slaCompliant = leadRepository.countSlaCompliantActiveLeads();

        // Null safe fallbacks
        if (totalSales == null) totalSales = BigDecimal.ZERO;
        if (avgMargin != null) {
            avgMargin = avgMargin.setScale(2, RoundingMode.HALF_UP);
        }

        // Mock chart data for now until we build the GROUP BY time-series queries
        List<SalesDataPoint> monthlySales = new ArrayList<>();
        monthlySales.add(new SalesDataPoint("Oct", new BigDecimal("120000")));
        monthlySales.add(new SalesDataPoint("Nov", new BigDecimal("185000")));
        monthlySales.add(new SalesDataPoint("Dec", new BigDecimal("145000")));
        monthlySales.add(new SalesDataPoint("Jan", new BigDecimal("210000")));
        monthlySales.add(new SalesDataPoint("Feb", new BigDecimal("190000")));
        monthlySales.add(new SalesDataPoint("Mar", new BigDecimal("280000")));

        List<ChartDataPoint> salesByBrand = new ArrayList<>();
        salesByBrand.add(new ChartDataPoint("Siemens", 450000, "#009999"));
        salesByBrand.add(new ChartDataPoint("ABB", 320000, "#ff0000"));
        salesByBrand.add(new ChartDataPoint("Schneider", 280000, "#3dcd58"));
        salesByBrand.add(new ChartDataPoint("L&T", 150000, "#ffcc00"));

        return KpiDashboardResponse.builder()
                .totalSales(totalSales)
                .totalOutstanding(totalOutstanding)
                .activeLeads(activeLeads)
                .averageMargin(avgMargin)
                .slaCompliantLeads(slaCompliant)
                .slaBreachedLeads(slaBreached)
                .monthlySales(monthlySales)
                .salesByBrand(salesByBrand)
                .build();
    }
}
