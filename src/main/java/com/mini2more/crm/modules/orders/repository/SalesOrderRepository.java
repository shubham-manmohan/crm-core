/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.orders.repository;
import com.mini2more.crm.modules.orders.entity.SalesOrder;

import com.mini2more.crm.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    @Query("SELECT o FROM SalesOrder o WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.clientName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.clientCompany) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR o.status = :status)")
    Page<SalesOrder> search(@Param("search") String search,
                             @Param("status") OrderStatus status,
                             Pageable pageable);

    @Query("SELECT MAX(o.id) FROM SalesOrder o")
    Long findMaxId();

    @Query("SELECT SUM(o.totalAmount) FROM SalesOrder o WHERE o.status NOT IN ('CANCELLED')")
    BigDecimal sumTotalSales();
}
