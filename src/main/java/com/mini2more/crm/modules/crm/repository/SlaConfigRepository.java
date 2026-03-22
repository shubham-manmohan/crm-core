/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.crm.repository;
import com.mini2more.crm.modules.crm.config.SlaConfig;

import com.mini2more.crm.common.enums.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlaConfigRepository extends JpaRepository<SlaConfig, Long> {
    Optional<SlaConfig> findByStageAndIsActiveTrue(LeadStatus stage);
}
