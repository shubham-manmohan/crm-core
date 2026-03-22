/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.inventory.repository;
import com.mini2more.crm.modules.inventory.entity.PurchaseOrder;

import com.mini2more.crm.common.enums.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("SELECT po FROM PurchaseOrder po WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR po.status = :status)")
    Page<PurchaseOrder> search(@Param("search") String search,
            @Param("status") PurchaseOrderStatus status,
            Pageable pageable);

    @Query("SELECT MAX(po.id) FROM PurchaseOrder po")
    Long findMaxId();
}
