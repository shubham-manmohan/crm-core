/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.master.entity;

import com.mini2more.crm.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "global_master_data", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "type", "code" })
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalMaster extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Category of master data: STATE, CITY, INDUSTRY, APPLICATION,
     * UNIT, GST_CODE, PAYMENT_TERM, LEAD_SOURCE, PRODUCT_CATEGORY, BRAND
     */
    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Parent reference for hierarchical data (e.g., City -> State)
     */
    @Column(name = "parent_code", length = 50)
    private String parentCode;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Extra metadata as JSON string (e.g., GST rate for GST codes, state code for
     * states)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
}
