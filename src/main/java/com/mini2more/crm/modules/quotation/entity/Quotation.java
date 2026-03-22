/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.quotation.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mini2more.crm.common.entity.BaseEntity;
import com.mini2more.crm.common.enums.QuotationStatus;
import com.mini2more.crm.modules.crm.entity.LeadEntity;
import com.mini2more.crm.modules.core.entity.Branch;
import com.mini2more.crm.modules.core.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quotation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quotation_number", unique = true, nullable = false, length = 25)
    private String quotationNumber;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "parent_quotation_id")
    private Long parentQuotationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "activities"})
    private LeadEntity lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private Rfq rfq;

    // Client info
    @Column(name = "client_name")
    private String clientName;

    @Column(name = "client_email")
    private String clientEmail;

    @Column(name = "client_phone")
    private String clientPhone;

    @Column(name = "client_company")
    private String clientCompany;

    @Column(name = "client_gst", length = 15)
    private String clientGst;

    @Column(name = "client_address", columnDefinition = "TEXT")
    private String clientAddress;

    // Assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prepared_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "password"})
    private UserEntity preparedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "password"})
    private UserEntity approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private Branch branch;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuotationStatus status;

    // Amounts
    @Column(name = "subtotal", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "gst_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal gstAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Margin
    @Column(name = "total_cost_price", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalCostPrice = BigDecimal.ZERO;

    @Column(name = "margin_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal marginPercent = BigDecimal.ZERO;

    @Column(name = "margin_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal marginAmount = BigDecimal.ZERO;

    // Terms
    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "delivery_terms")
    private String deliveryTerms;

    @Column(name = "validity_days")
    @Builder.Default
    private Integer validityDays = 30;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "warranty_terms")
    private String warrantyTerms;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "revision_notes", columnDefinition = "TEXT")
    private String revisionNotes;

    // Items
    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuotationItem> items = new ArrayList<>();

    // PDF path
    @Column(name = "pdf_path")
    private String pdfPath;
}
