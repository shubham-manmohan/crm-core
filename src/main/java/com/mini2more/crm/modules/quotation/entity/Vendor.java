/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.quotation.entity;

import com.mini2more.crm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vendors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vendor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column
    private String email;

    @Column
    private String phone;

    @Column(name = "alternate_phone")
    private String alternatePhone;

    @Column(name = "gst_number", length = 15)
    private String gstNumber;

    @Column
    private String address;

    @Column
    private String city;

    @Column
    private String state;

    @Column(length = 6)
    private String pincode;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "bank_ifsc", length = 11)
    private String bankIfsc;

    // Comma-separated brand codes the vendor supplies
    @Column(name = "brands_supplied", columnDefinition = "TEXT")
    private String brandsSupplied;

    // Comma-separated product category codes
    @Column(name = "product_categories", columnDefinition = "TEXT")
    private String productCategories;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Column(name = "rating")
    private Integer rating; // 1-5

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
