/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mini2more.crm.modules.core.entity.Branch;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Branch-wise inventory tracking.
 * Each record represents stock of a specific product at a specific branch.
 */
@Entity
@Table(name = "inventory", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "product_id", "branch_id" })
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private Branch branch;

    @Column(name = "available_qty", nullable = false)
    @Builder.Default
    private Integer availableQty = 0;

    @Column(name = "reserved_qty", nullable = false)
    @Builder.Default
    private Integer reservedQty = 0;

    @Column(name = "in_transit_qty", nullable = false)
    @Builder.Default
    private Integer inTransitQty = 0;

    @Column(name = "rack_location")
    private String rackLocation;

    @Column(name = "bin_number")
    private String binNumber;

    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Total stock = available + reserved (reserved is already part of physical
     * stock).
     */
    public int getTotalStock() {
        return (availableQty != null ? availableQty : 0)
                + (reservedQty != null ? reservedQty : 0);
    }

    /**
     * Check if stock is below reorder level.
     */
    public boolean isBelowReorderLevel() {
        return product != null && product.getReorderLevel() != null
                && getTotalStock() < product.getReorderLevel();
    }
}
