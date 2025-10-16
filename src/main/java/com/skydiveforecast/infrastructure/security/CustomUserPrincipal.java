package com.skydiveforecast.infrastructure.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public class CustomUserPrincipal implements UserDetails {

    @Getter
    private final Long userId;
    private final String username;
    private final String password;

    @Getter
    private final boolean isActive;

    private final Collection<? extends GrantedAuthority> authorities;

    @Getter
    private final Set<String> roles;

    @Getter
    private final Set<String> permissions;

    public CustomUserPrincipal(Long userId, String username, String password,
                               boolean isActive,
                               Collection<? extends GrantedAuthority> authorities) {
        this(userId, username, password, isActive, authorities, Set.of(), Set.of());
    }

    public CustomUserPrincipal(Long userId, String username, String password,
                               boolean isActive,
                               Collection<? extends GrantedAuthority> authorities,
                               Set<String> roles, Set<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.isActive = isActive;
        this.authorities = authorities;
        this.roles = roles;
        this.permissions = permissions;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}