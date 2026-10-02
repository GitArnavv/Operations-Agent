package com.aiops.security;

import com.aiops.domain.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {
    private final String id;
    private final String email;
    private final String fullName;
    private final String password;
    private final String tenantId;
    private final UserRole role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String id, String email, String fullName, String password, String tenantId, UserRole role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.tenantId = tenantId;
        this.role = role;
        java.util.List<GrantedAuthority> authList = new java.util.ArrayList<>();
        authList.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        if (role == UserRole.OWNER) {
            authList.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        this.authorities = java.util.Collections.unmodifiableList(authList);
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getTenantId() { return tenantId; }
    public UserRole getRole() { return role; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
