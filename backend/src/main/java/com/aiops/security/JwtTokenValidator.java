package com.aiops.security;

import com.aiops.domain.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Dedicated JWT Validator for stateless Spring Security authentication.
 * Enforces cryptographic signature verification, expiration check,
 * mandatory organization/tenant context, and RBAC role validation.
 */
@Component
public class JwtTokenValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenValidator.class);

    private final SecretKey key;

    public JwtTokenValidator(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates cryptographic signature and claims structure of a JWT token.
     *
     * @param token Bearer JWT token string
     * @return true if token is mathematically valid and unexpired with mandatory claims
     */
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            log.warn("JWT validation failed: Token is null or empty");
            return false;
        }

        try {
            Claims claims = extractAllClaims(token);

            // 1. Verify subject (User ID)
            if (claims.getSubject() == null || claims.getSubject().isBlank()) {
                log.warn("JWT validation failed: Missing subject claim");
                return false;
            }

            // 2. Verify mandatory Tenant ID (Organization context)
            String tenantId = claims.get("tenantId", String.class);
            if (tenantId == null || tenantId.isBlank()) {
                log.warn("JWT validation failed: Missing mandatory 'tenantId' organization claim");
                return false;
            }

            // 3. Verify valid RBAC Role claim
            String roleStr = claims.get("role", String.class);
            if (roleStr == null || roleStr.isBlank()) {
                log.warn("JWT validation failed: Missing mandatory 'role' RBAC claim");
                return false;
            }

            try {
                UserRole.valueOf(roleStr);
            } catch (IllegalArgumentException e) {
                log.warn("JWT validation failed: Unknown RBAC role '{}'", roleStr);
                return false;
            }

            // 4. Verify expiration
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                log.warn("JWT validation failed: Token expired at {}", expiration);
                return false;
            }

            return true;

        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature or malformed token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error validating JWT token: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Extracts verified claims payload from a signed JWT token.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Constructs an authenticated UserPrincipal from validated claims.
     */
    public UserPrincipal extractUserPrincipal(Claims claims) {
        String userId = claims.getSubject();
        String email = claims.get("email", String.class);
        String fullName = claims.get("fullName", String.class);
        String tenantId = claims.get("tenantId", String.class);
        String roleStr = claims.get("role", String.class);
        UserRole role = UserRole.valueOf(roleStr);

        return new UserPrincipal(userId, email, fullName, "", tenantId, role);
    }
}
