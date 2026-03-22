/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.core.repository;
import com.mini2more.crm.modules.core.entity.Company;

import com.mini2more.crm.common.enums.CompanyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query("SELECT c FROM Company c WHERE " +
           "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:type IS NULL OR c.companyType = :type)")
    Page<Company> search(@Param("search") String search, 
                         @Param("type") CompanyType type, 
                         Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
    Optional<Company> findByNameIgnoreCase(String name);
    boolean existsByEmailIgnoreCase(String email);
}
