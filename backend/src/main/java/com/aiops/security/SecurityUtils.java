package com.aiops.security;

import com.aiops.tenant.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return Optional.of((UserPrincipal) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    public static String getCurrentUserId() {
        return getCurrentUser().map(UserPrincipal::getId).orElse("system");
    }

    public static String getCurrentTenantId() {
        String threadTenant = TenantContext.getTenantId();
        if (threadTenant != null && !threadTenant.isBlank()) {
            return threadTenant;
        }
        return getCurrentUser().map(UserPrincipal::getTenantId).orElse("org_sharma_001");
    }
}
