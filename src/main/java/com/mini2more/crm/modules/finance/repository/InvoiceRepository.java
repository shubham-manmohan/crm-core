/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.finance.repository;
import com.mini2more.crm.modules.finance.entity.Invoice;

import com.mini2more.crm.common.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT i FROM Invoice i WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.clientName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.clientCompany) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR i.status = :status)")
    Page<Invoice> search(@Param("search") String search, @Param("status") InvoiceStatus status, Pageable pageable);

    @Query("SELECT MAX(i.id) FROM Invoice i")
    Long findMaxId();

    @Query("SELECT COALESCE(SUM(i.balanceDue), 0) FROM Invoice i WHERE i.status NOT IN ('CANCELLED', 'PAID')")
    BigDecimal sumTotalOutstanding();

    @Query("SELECT COALESCE(SUM(i.balanceDue), 0) FROM Invoice i WHERE " +
           "LOWER(i.clientCompany) = LOWER(:company) AND i.status NOT IN ('CANCELLED', 'PAID')")
    BigDecimal getOutstandingByCompany(@Param("company") String company);

    List<Invoice> findByStatusAndDueDateIsNotNull(InvoiceStatus status);
}
