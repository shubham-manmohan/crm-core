/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public JwtUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof JwtUserDetails) {
            return (JwtUserDetails) authentication.getDetails();
        }
        return null;
    }

    public Long getCurrentUserId() {
        JwtUserDetails user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    public String getCurrentUserRole() {
        JwtUserDetails user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    public String getCurrentUserEmail() {
        JwtUserDetails user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }
}
