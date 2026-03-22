/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.core.controller;

import com.mini2more.crm.config.BrandingConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/branding")
@RequiredArgsConstructor
public class BrandingController {

    private final BrandingConfig brandingConfig;

    @GetMapping
    public ResponseEntity<BrandingResponse> getBranding(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @RequestHeader(value = "Host", required = false) String host) {
        
        // Multi-tenant resolution logic
        // 1. Check strict Tenant ID header
        // 2. Fallback to extracting subdomain from Host header
        // 3. Fallback to default Mini2More identity

        String effectiveTenant = "default";
        
        if (tenantId != null && !tenantId.isEmpty()) {
            effectiveTenant = tenantId;
        } else if (host != null && host.contains(".")) {
            String subdomain = host.split("\\.")[0];
            if (!subdomain.equals("www") && !subdomain.equals("localhost")) {
                effectiveTenant = subdomain;
            }
        }

        BrandingResponse response;
        if (brandingConfig.getTenants() != null && brandingConfig.getTenants().containsKey(effectiveTenant)) {
            BrandingConfig.TenantBranding tb = brandingConfig.getTenants().get(effectiveTenant);
            response = new BrandingResponse(
                tb.getClientName(), 
                tb.getLogoUrl(), 
                tb.getPrimaryColor(), 
                tb.getSupportEmail()
            );
        } else {
            response = new BrandingResponse(
                brandingConfig.getDefaultClientName(),
                brandingConfig.getDefaultLogoUrl(),
                brandingConfig.getDefaultPrimaryColor(),
                brandingConfig.getDefaultSupportEmail()
            );
        }

        return ResponseEntity.ok(response);
    }

    // DTO for response
    public record BrandingResponse(
        String clientName,
        String logoUrl,
        String primaryColor,
        String supportEmail
    ) {}
}
