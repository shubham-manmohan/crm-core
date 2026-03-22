/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.orders.repository;
import com.mini2more.crm.modules.orders.entity.Dispatch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DispatchRepository extends JpaRepository<Dispatch, Long> {

    List<Dispatch> findBySalesOrderId(Long orderId);

    @Query("SELECT d FROM Dispatch d WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(d.dispatchNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.courierName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.trackingNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Dispatch> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT MAX(d.id) FROM Dispatch d")
    Long findMaxId();
}
