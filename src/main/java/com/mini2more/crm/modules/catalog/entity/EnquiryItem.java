/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.catalog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mini2more.crm.modules.inventory.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "enquiry_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class EnquiryItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_id", nullable = false)
    @JsonIgnore
    private Enquiry enquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private Product product;

    @Column(name = "product_name") private String productName;
    @Column(name = "product_code") private String productCode;
    @Column private String brand;
    @Column private Integer quantity;
    @Column private String unit;
    @Column(columnDefinition = "TEXT") private String specifications;
}
