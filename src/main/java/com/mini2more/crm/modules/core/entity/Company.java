/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.core.entity;

import com.mini2more.crm.common.entity.BaseEntity;
import com.mini2more.crm.common.enums.CompanyType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "companies", indexes = {
    @Index(name = "idx_company_name", columnList = "name"),
    @Index(name = "idx_company_type", columnList = "company_type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_type", nullable = false)
    private CompanyType companyType;

    @Column(unique = true)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "gst_number", length = 15)
    private String gstNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String city;
    private String state;

    @Column(length = 10)
    private String pincode;

    private String website;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
