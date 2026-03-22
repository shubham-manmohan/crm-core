/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.config;

import com.mini2more.crm.security.JwtUserDetails;
import com.mini2more.crm.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final SecurityUtils securityUtils;

    public void log(String action, String entityType, Long entityId, String details) {
        JwtUserDetails user = securityUtils.getCurrentUser();
        AuditLog auditLog = AuditLog.builder()
                .userId(user != null ? user.getUserId() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();
        auditLogRepository.save(auditLog);
    }

    public void log(String action, String entityType, Long entityId, String details, String ipAddress) {
        JwtUserDetails user = securityUtils.getCurrentUser();
        AuditLog auditLog = AuditLog.builder()
                .userId(user != null ? user.getUserId() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(auditLog);
    }
}
