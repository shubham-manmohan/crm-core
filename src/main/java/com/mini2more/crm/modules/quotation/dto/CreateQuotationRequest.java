/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.quotation.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateQuotationRequest {
    private Long leadId;
    private Long rfqId;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String clientCompany;
    private String clientGst;
    private String clientAddress;
    private Long branchId;
    private String paymentTerms;
    private String deliveryTerms;
    private Integer validityDays;
    private String warrantyTerms;
    private String termsAndConditions;
    private String notes;
    private BigDecimal discountPercent;
    private List<QuotationItemDto> items;
}
