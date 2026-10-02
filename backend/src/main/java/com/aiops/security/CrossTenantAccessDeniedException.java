package com.aiops.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an authenticated principal attempts to access or mutate records
 * belonging to an organization/tenant other than their own.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class CrossTenantAccessDeniedException extends AccessDeniedException {

    public CrossTenantAccessDeniedException(String msg) {
        super(msg);
    }

    public CrossTenantAccessDeniedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
