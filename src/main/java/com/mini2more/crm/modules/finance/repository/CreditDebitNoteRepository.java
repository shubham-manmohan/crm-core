/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.finance.repository;
import com.mini2more.crm.modules.finance.entity.CreditDebitNote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CreditDebitNoteRepository extends JpaRepository<CreditDebitNote, Long> {
    List<CreditDebitNote> findByInvoiceId(Long invoiceId);
    @Query("SELECT MAX(c.id) FROM CreditDebitNote c")
    Long findMaxId();
}
