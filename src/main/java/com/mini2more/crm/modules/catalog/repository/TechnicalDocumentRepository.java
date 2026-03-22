/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.catalog.repository;
import com.mini2more.crm.modules.catalog.entity.TechnicalDocument;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TechnicalDocumentRepository extends JpaRepository<TechnicalDocument, Long> {

    @Query("SELECT d FROM TechnicalDocument d WHERE d.isPublic = true AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(d.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.productCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TechnicalDocument> searchPublic(@Param("search") String search, Pageable pageable);
}
