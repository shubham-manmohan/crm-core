/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.quotation.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuotationItemDto {
    private Long productId;
    private String productName;
    private String productCode;
    private String brand;
    private String category;
    private String description;
    private String unit;
    private Integer quantity;
    private BigDecimal costPrice;
    private BigDecimal unitPrice;
    private BigDecimal discountPercent;
    private BigDecimal gstPercent;
    private String hsnCode;
    private Integer deliveryDays;
    private Long vendorId;
    private String vendorName;
}
