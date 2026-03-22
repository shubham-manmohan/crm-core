/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.finance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InvoiceItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore
    private Invoice invoice;

    @Column(name = "product_name", nullable = false) private String productName;
    @Column(name = "product_code") private String productCode;
    @Column(name = "hsn_code", length = 8) private String hsnCode;
    @Column private String unit;
    @Column(nullable = false) private Integer quantity;

    @Column(name = "unit_price", precision = 15, scale = 2) @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "discount_percent", precision = 5, scale = 2) @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "taxable_amount", precision = 15, scale = 2) @Builder.Default
    private BigDecimal taxableAmount = BigDecimal.ZERO;

    @Column(name = "gst_percent", precision = 5, scale = 2) @Builder.Default
    private BigDecimal gstPercent = new BigDecimal("18.00");

    @Column(name = "cgst_amount", precision = 15, scale = 2) @Builder.Default
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(name = "sgst_amount", precision = 15, scale = 2) @Builder.Default
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(name = "igst_amount", precision = 15, scale = 2) @Builder.Default
    private BigDecimal igstAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 15, scale = 2) @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
