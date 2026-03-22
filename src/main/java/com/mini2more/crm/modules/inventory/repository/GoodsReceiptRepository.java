/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.inventory.repository;
import com.mini2more.crm.modules.inventory.entity.GoodsReceipt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    @Query("SELECT MAX(g.id) FROM GoodsReceipt g")
    Long findMaxId();
}
