/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "mini2more.branding")
public class BrandingConfig {
    
    // A map to hold different clients if driven by properties, or just a default fallback
    private String defaultClientName = "Mini2More CRM";
    private String defaultLogoUrl = "/logo.png";
    private String defaultPrimaryColor = "#0f172a";
    private String defaultSupportEmail = "support@mini2more.com";

    // Simulating a multi-tenant DB structure via a map of tenant-id string to details
    // In production, this would hit the DB.
    private Map<String, TenantBranding> tenants;

    @Data
    public static class TenantBranding {
        private String clientName;
        private String logoUrl;
        private String primaryColor;
        private String supportEmail;
    }
}
