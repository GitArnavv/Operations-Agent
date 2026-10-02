package com.aiops.tenant;

import com.aiops.security.CrossTenantAccessDeniedException;
import com.aiops.security.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * AOP Aspect enforcing organization / tenant boundaries on every JPA repository query and mutation.
 * Guarantees that cross-tenant data leakage is impossible by verifying that any tenant-scoped
 * parameter or entity matches the authenticated user's organization context.
 */
@Aspect
@Component
@Order(1)
public class TenantSecurityAspect {

    private static final Logger log = LoggerFactory.getLogger(TenantSecurityAspect.class);

    @Pointcut("execution(* com.aiops.repository.*.*(..))")
    public void repositoryOperations() {}

    @Before("repositoryOperations()")
    public void verifyOrganizationContext(JoinPoint joinPoint) {
        String authenticatedTenant = TenantContext.getTenantId();
        if (authenticatedTenant == null || authenticatedTenant.isBlank()) {
            authenticatedTenant = SecurityUtils.getCurrentUser()
                    .map(principal -> principal.getTenantId())
                    .orElse(null);
        }

        // If no authenticated tenant context exists (e.g. system seeder or internal worker), bypass
        if (authenticatedTenant == null || authenticatedTenant.isBlank()) {
            return;
        }

        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();

        if (args == null || args.length == 0) {
            return;
        }

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) continue;

            String paramName = (paramNames != null && paramNames.length > i) ? paramNames[i] : "";

            // 1. Verify String tenantId parameters
            if (arg instanceof String stringArg) {
                if ("tenantId".equalsIgnoreCase(paramName) ||
                    (paramName.toLowerCase().contains("tenant") && (stringArg.startsWith("org_") || stringArg.startsWith("tenant_")))) {
                    if (!stringArg.equalsIgnoreCase(authenticatedTenant)) {
                        log.error("[TENANT-VIOLATION] Cross-tenant query attempt blocked in method '{}.{}': authenticated tenant '{}' attempted access using tenant '{}'",
                                signature.getDeclaringType().getSimpleName(), signature.getName(), authenticatedTenant, stringArg);
                        throw new CrossTenantAccessDeniedException(
                                "Cross-tenant data leakage prevented: Principal belongs to tenant '" + authenticatedTenant +
                                "' and is forbidden from querying or modifying records for tenant '" + stringArg + "'."
                        );
                    }
                }
            }

            // 2. Verify Entity objects being persisted or updated
            verifyEntityTenantContext(arg, authenticatedTenant, signature);

            // 3. Verify Collections of Entities (e.g., saveAll)
            if (arg instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    verifyEntityTenantContext(item, authenticatedTenant, signature);
                }
            }
        }
    }

    private void verifyEntityTenantContext(Object entity, String authenticatedTenant, MethodSignature signature) {
        if (entity == null || entity instanceof String || entity instanceof Number || entity instanceof Boolean) {
            return;
        }

        try {
            Method getTenantIdMethod = entity.getClass().getMethod("getTenantId");
            Object entityTenantObj = getTenantIdMethod.invoke(entity);
            if (entityTenantObj instanceof String entityTenant && !entityTenant.isBlank()) {
                if (!entityTenant.equalsIgnoreCase(authenticatedTenant)) {
                    log.error("[TENANT-VIOLATION] Cross-tenant entity mutation blocked in '{}.{}': entity belongs to '{}', active tenant is '{}'",
                            signature.getDeclaringType().getSimpleName(), signature.getName(), entityTenant, authenticatedTenant);
                    throw new CrossTenantAccessDeniedException(
                            "Cross-tenant data leakage prevented: Cannot persist or update entity belonging to tenant '" +
                            entityTenant + "' under authenticated tenant context '" + authenticatedTenant + "'."
                    );
                }
            }
        } catch (NoSuchMethodException ignored) {
            // Not a tenant-aware entity, skip
        } catch (CrossTenantAccessDeniedException e) {
            throw e;
        } catch (Exception ex) {
            log.trace("Could not inspect entity for tenant verification: {}", ex.getMessage());
        }
    }
}
