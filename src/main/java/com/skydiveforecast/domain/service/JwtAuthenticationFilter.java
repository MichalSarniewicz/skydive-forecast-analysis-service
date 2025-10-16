package com.skydiveforecast.domain.service;

import com.skydiveforecast.infrastructure.security.CustomUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(AuthService authService, UserDetailsService userDetailsService) {
        this.authService = authService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = authService.extractUsername(jwt);

                if (!authService.isTokenValid(jwt)) {
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                logger.error("Error processing JWT token", e);
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (authService.validateToken(jwt, userDetails)) {
                Set<String> roles = authService.extractRoles(jwt);
                Set<String> permissions = authService.extractPermissions(jwt);

                if (userDetails instanceof CustomUserPrincipal customUserPrincipal) {
                    CustomUserPrincipal enhancedPrincipal = new CustomUserPrincipal(
                            customUserPrincipal.getUserId(),
                            customUserPrincipal.getUsername(),
                            customUserPrincipal.getPassword(),
                            customUserPrincipal.isActive(),
                            customUserPrincipal.getAuthorities(),
                            roles,
                            permissions
                    );

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            enhancedPrincipal, null, enhancedPrincipal.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
