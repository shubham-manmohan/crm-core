/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.inventory.repository;
import com.mini2more.crm.modules.inventory.entity.InventoryItem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByProductIdAndBranchId(Long productId, Long branchId);

    List<InventoryItem> findByProductId(Long productId);

    List<InventoryItem> findByBranchId(Long branchId);

    @Query("SELECT i FROM InventoryItem i JOIN FETCH i.product p JOIN FETCH i.branch b WHERE " +
            "(:branchId IS NULL OR b.id = :branchId) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.productCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<InventoryItem> searchInventory(@Param("search") String search,
            @Param("branchId") Long branchId,
            Pageable pageable);

    // Low stock: available + reserved < reorder level
    @Query("SELECT i FROM InventoryItem i JOIN FETCH i.product p WHERE " +
            "(i.availableQty + i.reservedQty) < p.reorderLevel " +
            "AND (:branchId IS NULL OR i.branch.id = :branchId)")
    List<InventoryItem> findLowStockItems(@Param("branchId") Long branchId);
}
