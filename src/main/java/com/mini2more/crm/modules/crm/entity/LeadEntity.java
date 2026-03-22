/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mini2more.crm.common.entity.BaseEntity;
import com.mini2more.crm.common.enums.LeadPriority;
import com.mini2more.crm.common.enums.LeadStatus;
import com.mini2more.crm.modules.core.entity.Branch;
import com.mini2more.crm.modules.core.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "leads", indexes = {
    @Index(name = "idx_lead_number", columnList = "lead_number"),
    @Index(name = "idx_lead_status", columnList = "status"),
    @Index(name = "idx_lead_client_email", columnList = "client_email")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lead_number", unique = true, nullable = false, length = 20)
    private String leadNumber;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // --- Client / Customer ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "password"})
    private UserEntity client;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "client_email")
    private String clientEmail;

    @Column(name = "client_phone")
    private String clientPhone;

    @Column(name = "client_company")
    private String clientCompany;

    // --- Assignment ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "password"})
    private UserEntity assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private Branch branch;

    // --- Status & Priority ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadPriority priority;

    // --- Source & Category ---
    @Column(name = "source")
    private String source;

    @Column(name = "industry")
    private String industry;

    @Column(name = "application")
    private String application;

    // --- Value ---
    @Column(name = "estimated_value", precision = 15, scale = 2)
    private BigDecimal estimatedValue;

    // --- SLA ---
    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Column(name = "sla_breached")
    @Builder.Default
    private Boolean slaBreached = false;

    @Column(name = "escalation_level")
    @Builder.Default
    private Integer escalationLevel = 0;

    // --- Follow-up ---
    @Column(name = "next_follow_up")
    private LocalDateTime nextFollowUp;

    @Column(name = "follow_up_notes", columnDefinition = "TEXT")
    private String followUpNotes;

    // --- Lost reason ---
    @Column(name = "lost_reason")
    private String lostReason;

    // --- Products / Requirements ---
    @Column(name = "product_requirements", columnDefinition = "TEXT")
    private String productRequirements;

    @Column(name = "quantity")
    private String quantity;

    @Column(name = "brand_preference")
    private String brandPreference;

    // --- Attachments ---
    @Column(name = "attachment_paths", columnDefinition = "TEXT")
    private String attachmentPaths;

    // --- Activities ---
    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LeadActivity> activities = new ArrayList<>();

    // --- Tags ---
    @Column(name = "tags")
    private String tags;

    @Column(name = "project_name")
    private String projectName;
}
