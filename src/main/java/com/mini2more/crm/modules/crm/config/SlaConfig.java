/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.crm.config;

import com.mini2more.crm.common.enums.LeadStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sla_config")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The lead stage this SLA applies to
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private LeadStatus stage;

    /**
     * Maximum hours allowed for this stage before SLA breach
     */
    @Column(name = "max_hours", nullable = false)
    private Integer maxHours;

    /**
     * Escalation level 1 role (e.g., SALES)
     */
    @Column(name = "escalation_level_1", length = 30)
    private String escalationLevel1;

    /**
     * Escalation level 2 role (e.g., ADMIN as Branch Manager)
     */
    @Column(name = "escalation_level_2", length = 30)
    private String escalationLevel2;

    /**
     * Escalation level 3 role (e.g., ADMIN)
     */
    @Column(name = "escalation_level_3", length = 30)
    private String escalationLevel3;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
