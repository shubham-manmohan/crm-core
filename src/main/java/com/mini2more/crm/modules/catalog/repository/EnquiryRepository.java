/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.catalog.repository;
import com.mini2more.crm.modules.catalog.entity.Enquiry;

import com.mini2more.crm.common.enums.EnquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {

    @Query("SELECT e FROM Enquiry e WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(e.enquiryNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.contactName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.companyName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR e.status = :status)")
    Page<Enquiry> search(@Param("search") String search, @Param("status") EnquiryStatus status, Pageable pageable);

    Page<Enquiry> findByCustomerId(Long customerId, Pageable pageable);

    @Query("SELECT MAX(e.id) FROM Enquiry e")
    Long findMaxId();
}
