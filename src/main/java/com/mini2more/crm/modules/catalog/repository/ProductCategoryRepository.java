/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.catalog.repository;
import com.mini2more.crm.modules.catalog.entity.ProductCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    List<ProductCategory> findByIsActiveTrueOrderByDisplayOrder();
    List<ProductCategory> findByParentIdAndIsActiveTrue(Long parentId);
    List<ProductCategory> findByParentIsNullAndIsActiveTrueOrderByDisplayOrder();
}
