/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.master.repository;
import com.mini2more.crm.modules.master.entity.TenantMaster;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TenantMasterRepository extends JpaRepository<TenantMaster, Long> {

    List<TenantMaster> findByTypeAndIsActiveTrueOrderByDisplayOrderAscNameAsc(String type);

    List<TenantMaster> findByTypeAndParentCodeAndIsActiveTrueOrderByDisplayOrderAscNameAsc(String type,
            String parentCode);

    Optional<TenantMaster> findByTypeAndCode(String type, String code);

    boolean existsByTypeAndCode(String type, String code);

    @Query("SELECT m FROM TenantMaster m WHERE m.type = :type AND m.isActive = true " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<TenantMaster> searchByType(@Param("type") String type, @Param("search") String search);

    long countByType(String type);
}
