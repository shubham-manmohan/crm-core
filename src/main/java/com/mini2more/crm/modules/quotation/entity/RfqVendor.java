/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.quotation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks each vendor's response to an RFQ (multi-vendor price comparison).
 */
@Entity
@Table(name = "rfq_vendors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RfqVendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Rfq rfq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column
    private Integer quantity;

    @Column(name = "delivery_days")
    private Integer deliveryDays;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "warranty_months")
    private Integer warrantyMonths;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_selected")
    @Builder.Default
    private Boolean isSelected = false;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
