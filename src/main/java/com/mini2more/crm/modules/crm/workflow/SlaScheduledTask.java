/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.crm.workflow;
import com.mini2more.crm.modules.crm.entity.LeadActivity;
import com.mini2more.crm.modules.crm.entity.LeadEntity;
import com.mini2more.crm.modules.crm.repository.SlaConfigRepository;
import com.mini2more.crm.modules.crm.repository.SlaLogRepository;
import com.mini2more.crm.modules.crm.entity.SlaLog;
import com.mini2more.crm.modules.crm.repository.LeadActivityRepository;
import com.mini2more.crm.modules.crm.repository.LeadRepository;

import com.mini2more.crm.common.enums.LeadStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task that runs every 15 minutes to check for SLA breaches
 * and escalate leads according to the escalation matrix.
 *
 * Escalation levels (as per BRD 9.4):
 * Level 1 – Sales Representative (assigned user)
 * Level 2 – Branch Manager (Admin)
 * Level 3 – Admin
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SlaScheduledTask {

    private final LeadRepository leadRepository;
    private final SlaConfigRepository slaConfigRepository;
    private final SlaLogRepository slaLogRepository;
    private final LeadActivityRepository activityRepository;

    @Scheduled(fixedDelay = 900000) // 15 minutes
    @Transactional
    public void checkSlaBreaches() {
        LocalDateTime now = LocalDateTime.now();
        List<LeadEntity> breachingLeads = leadRepository.findLeadsBreachingSla(now);

        if (breachingLeads.isEmpty())
            return;

        log.info("SLA Check: Found {} leads breaching SLA", breachingLeads.size());

        for (LeadEntity lead : breachingLeads) {
            lead.setSlaBreached(true);
            int currentLevel = lead.getEscalationLevel() != null ? lead.getEscalationLevel() : 0;
            int newLevel = Math.min(currentLevel + 1, 3);
            lead.setEscalationLevel(newLevel);

            // Determine escalation target
            String escalatedTo = getEscalationTarget(lead.getStatus(), newLevel);

            // Log SLA breach
            SlaLog slaLog = SlaLog.builder()
                    .lead(lead)
                    .stage(lead.getStatus())
                    .breachedAt(now)
                    .escalationLevel(newLevel)
                    .escalatedTo(escalatedTo)
                    .notes("SLA breached at level " + newLevel + " for stage " + lead.getStatus())
                    .build();
            slaLogRepository.save(slaLog);

            // Add activity
            LeadActivity activity = LeadActivity.builder()
                    .lead(lead)
                    .type("ESCALATION")
                    .description("SLA BREACH: Lead escalated to Level " + newLevel
                            + " (" + escalatedTo + ") for stage " + lead.getStatus())
                    .oldValue("Level " + currentLevel)
                    .newValue("Level " + newLevel)
                    .build();
            activityRepository.save(activity);

            leadRepository.save(lead);
            log.warn("SLA BREACH: Lead {} escalated to level {} ({})",
                    lead.getLeadNumber(), newLevel, escalatedTo);
        }
    }

    private String getEscalationTarget(LeadStatus stage, int level) {
        return slaConfigRepository.findByStageAndIsActiveTrue(stage)
                .map(config -> {
                    switch (level) {
                        case 1:
                            return config.getEscalationLevel1() != null
                                    ? config.getEscalationLevel1()
                                    : "SALES";
                        case 2:
                            return config.getEscalationLevel2() != null
                                    ? config.getEscalationLevel2()
                                    : "ADMIN";
                        case 3:
                            return config.getEscalationLevel3() != null
                                    ? config.getEscalationLevel3()
                                    : "ADMIN";
                        default:
                            return "ADMIN";
                    }
                })
                .orElse(level <= 1 ? "SALES" : "ADMIN");
    }
}
