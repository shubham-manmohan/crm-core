/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.quotation.repository;
import com.mini2more.crm.modules.quotation.entity.Quotation;

import com.mini2more.crm.common.enums.QuotationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    @Query("SELECT q FROM Quotation q WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(q.quotationNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.clientName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.clientCompany) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR q.status = :status)")
    Page<Quotation> searchQuotations(@Param("search") String search,
            @Param("status") QuotationStatus status,
            Pageable pageable);

    List<Quotation> findByLeadIdOrderByVersionDesc(Long leadId);

    List<Quotation> findByParentQuotationIdOrderByVersionDesc(Long parentId);

    long countByStatus(QuotationStatus status);

    @Query("SELECT MAX(q.id) FROM Quotation q")
    Long findMaxId();

    @Query("SELECT COALESCE(AVG(q.marginPercent), 0) FROM Quotation q WHERE q.status = 'APPROVED' OR q.status = 'ACCEPTED'")
    java.math.BigDecimal findAverageMargin();
}
