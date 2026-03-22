/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.orders.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dispatch_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatch_id", nullable = false)
    @JsonIgnore
    private Dispatch dispatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    @JsonIgnore
    private OrderItem orderItem;

    @Column(name = "dispatched_qty", nullable = false)
    private Integer dispatchedQty;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_code")
    private String productCode;
}
