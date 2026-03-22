/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.catalog.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mini2more.crm.common.entity.BaseEntity;
import com.mini2more.crm.common.enums.EnquiryStatus;
import com.mini2more.crm.modules.core.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enquiries")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Enquiry extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enquiry_number", unique = true, nullable = false, length = 20)
    private String enquiryNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "password"})
    private UserEntity customer;

    // Contact info (for guest enquiries)
    @Column(name = "contact_name") private String contactName;
    @Column(name = "contact_email") private String contactEmail;
    @Column(name = "contact_phone") private String contactPhone;
    @Column(name = "company_name") private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnquiryStatus status;

    @Column(name = "enquiry_type") private String enquiryType; // STANDARD, PROJECT, BULK_RFQ
    @Column(name = "project_name") private String projectName;
    @Column(name = "project_description", columnDefinition = "TEXT") private String projectDescription;
    @Column(columnDefinition = "TEXT") private String requirements;
    @Column(name = "urgency_level") private String urgencyLevel; // NORMAL, URGENT, CRITICAL
    @Column(name = "boq_file_path") private String boqFilePath;

    @OneToMany(mappedBy = "enquiry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EnquiryItem> items = new ArrayList<>();
}
