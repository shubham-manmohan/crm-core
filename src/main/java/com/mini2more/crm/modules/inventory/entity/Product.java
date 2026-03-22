/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.inventory.entity;

import com.mini2more.crm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", unique = true, nullable = false, length = 30)
    private String productCode;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String brand;

    @Column
    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    @Column
    private String unit;

    @Column(name = "hsn_code", length = 8)
    private String hsnCode;

    @Column(name = "gst_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal gstPercent = new BigDecimal("18.00");

    @Column(name = "mrp", precision = 15, scale = 2)
    private BigDecimal mrp;

    @Column(name = "selling_price", precision = 15, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "min_order_qty")
    @Builder.Default
    private Integer minOrderQty = 1;

    @Column(name = "reorder_level")
    @Builder.Default
    private Integer reorderLevel = 10;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "datasheet_path")
    private String datasheetPath;

    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;

    @Column(name = "weight_kg")
    private String weightKg;

    @Column(name = "dimensions")
    private String dimensions;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "tags")
    private String tags;
}
