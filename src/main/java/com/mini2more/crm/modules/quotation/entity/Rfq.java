/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.quotation.entity;

import com.mini2more.crm.common.entity.BaseEntity;
import com.mini2more.crm.common.enums.RfqStatus;
import com.mini2more.crm.modules.crm.entity.LeadEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rfqs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rfq extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rfq_number", unique = true, nullable = false, length = 20)
    private String rfqNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private LeadEntity lead;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requirements;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RfqStatus status;

    @Column(name = "response_deadline")
    private LocalDate responseDeadline;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "rfq", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RfqVendor> rfqVendors = new ArrayList<>();
}
