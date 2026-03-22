/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.finance.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mini2more.crm.common.entity.BaseEntity;
import com.mini2more.crm.common.enums.InvoiceStatus;
import com.mini2more.crm.modules.orders.entity.SalesOrder;
import com.mini2more.crm.modules.core.entity.Branch;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_invoice_number", columnList = "invoice_number"),
    @Index(name = "idx_invoice_status", columnList = "status")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Invoice extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", unique = true, nullable = false, length = 20)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "items"})
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private Branch branch;

    // Client info
    @Column(name = "client_name") private String clientName;
    @Column(name = "client_company") private String clientCompany;
    @Column(name = "client_gst", length = 15) private String clientGst;
    @Column(name = "client_address", columnDefinition = "TEXT") private String clientAddress;
    @Column(name = "client_state") private String clientState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Column(name = "invoice_date") private LocalDate invoiceDate;
    @Column(name = "due_date") private LocalDate dueDate;

    // GST breakup
    @Column(name = "taxable_amount", precision = 15, scale = 2) @Builder.Default
    private BigDecimal taxableAmount = BigDecimal.ZERO;

    @Column(name = "cgst_amount", precision = 15, scale = 2) @Builder.Default
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(name = "sgst_amount", precision = 15, scale = 2) @Builder.Default
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(name = "igst_amount", precision = 15, scale = 2) @Builder.Default
    private BigDecimal igstAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 15, scale = 2) @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", precision = 15, scale = 2) @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "balance_due", precision = 15, scale = 2) @Builder.Default
    private BigDecimal balanceDue = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2) @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "round_off", precision = 10, scale = 2) @Builder.Default
    private BigDecimal roundOff = BigDecimal.ZERO;

    // Flags
    @Column(name = "is_igst") @Builder.Default
    private Boolean isIgst = false;

    @Column(columnDefinition = "TEXT") private String notes;
    @Column(name = "terms_conditions", columnDefinition = "TEXT") private String termsConditions;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();
}
