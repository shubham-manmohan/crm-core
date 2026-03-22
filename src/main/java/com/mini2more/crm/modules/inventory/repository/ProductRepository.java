/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.inventory.repository;
import com.mini2more.crm.modules.inventory.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.productCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:brand IS NULL OR :brand = '' OR p.brand = :brand) " +
            "AND (:category IS NULL OR :category = '' OR p.category = :category)")
    Page<Product> searchProducts(@Param("search") String search,
            @Param("brand") String brand,
            @Param("category") String category,
            Pageable pageable);

    boolean existsByProductCode(String productCode);
}
