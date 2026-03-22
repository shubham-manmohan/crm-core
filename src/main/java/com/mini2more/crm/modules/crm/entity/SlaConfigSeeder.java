/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.crm.entity;
import com.mini2more.crm.modules.crm.repository.SlaConfigRepository;
import com.mini2more.crm.modules.crm.config.SlaConfig;

import com.mini2more.crm.common.enums.LeadStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class SlaConfigSeeder implements CommandLineRunner {

    private final SlaConfigRepository slaConfigRepository;

    @Override
    public void run(String... args) {
        if (slaConfigRepository.count() > 0)
            return;

        // SLA config per lead stage (hours, escalation targets)
        seedSla(LeadStatus.NEW, 2, "SALES", "ADMIN", "ADMIN");
        seedSla(LeadStatus.ASSIGNED, 4, "SALES", "ADMIN", "ADMIN");
        seedSla(LeadStatus.IN_DISCUSSION, 24, "SALES", "ADMIN", "ADMIN");
        seedSla(LeadStatus.RFQ_SENT, 48, "PURCHASER", "ADMIN", "ADMIN");
        seedSla(LeadStatus.QUOTATION_PREPARED, 8, "SALES", "ADMIN", "ADMIN");
        seedSla(LeadStatus.QUOTATION_SENT, 72, "SALES", "ADMIN", "ADMIN");
        seedSla(LeadStatus.NEGOTIATION, 120, "SALES", "ADMIN", "ADMIN");

        log.info("✅ SLA config seeded for 7 lead stages");
    }

    private void seedSla(LeadStatus stage, int maxHours,
            String level1, String level2, String level3) {
        slaConfigRepository.save(SlaConfig.builder()
                .stage(stage)
                .maxHours(maxHours)
                .escalationLevel1(level1)
                .escalationLevel2(level2)
                .escalationLevel3(level3)
                .isActive(true)
                .build());
    }
}
