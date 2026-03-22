/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.quotation.repository;
import com.mini2more.crm.modules.quotation.entity.Vendor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VendorRepository extends JpaRepository<Vendor, Long> {

    @Query("SELECT v FROM Vendor v WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.contactPerson) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.gstNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.brandsSupplied) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Vendor> searchVendors(@Param("search") String search, Pageable pageable);

    boolean existsByGstNumber(String gstNumber);
}
