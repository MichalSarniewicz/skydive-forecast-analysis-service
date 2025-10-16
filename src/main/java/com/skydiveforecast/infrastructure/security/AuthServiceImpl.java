package com.skydiveforecast.infrastructure.security;

import com.skydiveforecast.domain.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${jwt.secret}")
    private String secretKey;


    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) authentication.getPrincipal()).getUserId();
        }
        return null;
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    @Override
    public Set<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        List<String> rolesList = claims.get("roles", List.class);
        return rolesList != null ? new HashSet<>(rolesList) : new HashSet<>();
    }

    @Override
    public Set<String> extractPermissions(String token) {
        Claims claims = extractAllClaims(token);
        List<String> permissionsList = claims.get("permissions", List.class);
        return permissionsList != null ? new HashSet<>(permissionsList) : new HashSet<>();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}

