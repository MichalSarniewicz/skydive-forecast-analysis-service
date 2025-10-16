package com.skydiveforecast.domain.service;

import com.skydiveforecast.infrastructure.security.CustomUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PermissionSecurityService {

    private final AuthService authService;

    public boolean hasPermission(String permission) {
        // First, try to get permissions from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserPrincipal customUserPrincipal) {
                // If we've already extracted permissions during authentication, use them
                if (customUserPrincipal.getPermissions() != null) {
                    return customUserPrincipal.getPermissions().contains(permission);
                }
            }
        }

        // Fallback to extracting permissions from the token
        try {
            String token = extractTokenFromRequest();
            if (token == null || !authService.isTokenValid(token)) {
                return false;
            }

            Set<String> userPermissions = authService.extractPermissions(token);
            return userPermissions.contains(permission);

        } catch (Exception e) {
            return false;
        }
    }

    private String extractTokenFromRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }

        return null;
    }
}