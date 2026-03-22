/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.quotation.repository;
import com.mini2more.crm.modules.quotation.entity.Rfq;

import com.mini2more.crm.common.enums.RfqStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RfqRepository extends JpaRepository<Rfq, Long> {

    @Query("SELECT r FROM Rfq r WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(r.rfqNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.requirements) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR r.status = :status)")
    Page<Rfq> searchRfqs(@Param("search") String search,
            @Param("status") RfqStatus status,
            Pageable pageable);

    @Query("SELECT MAX(r.id) FROM Rfq r")
    Long findMaxId();
}
