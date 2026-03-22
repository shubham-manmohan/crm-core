/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.inventory.repository;
import com.mini2more.crm.modules.inventory.entity.StockTransfer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {

    @Query("SELECT st FROM StockTransfer st WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(st.transferNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<StockTransfer> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT MAX(st.id) FROM StockTransfer st")
    Long findMaxId();
}
