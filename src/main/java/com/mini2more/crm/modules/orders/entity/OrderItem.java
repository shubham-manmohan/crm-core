/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.orders.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mini2more.crm.modules.inventory.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private Product product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_code")
    private String productCode;

    @Column
    private String brand;

    @Column
    private String unit;

    @Column(name = "ordered_qty", nullable = false)
    private Integer orderedQty;

    @Column(name = "dispatched_qty")
    @Builder.Default
    private Integer dispatchedQty = 0;

    @Column(name = "delivered_qty")
    @Builder.Default
    private Integer deliveredQty = 0;

    @Column(name = "unit_price", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "gst_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal gstPercent = new BigDecimal("18.00");

    @Column(name = "total_price", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(name = "hsn_code", length = 8)
    private String hsnCode;

    public int getPendingQty() {
        return orderedQty - (dispatchedQty != null ? dispatchedQty : 0);
    }
}
