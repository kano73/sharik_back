package com.mary.sharik.config.security.jwt;

import com.mary.sharik.model.details.MyUserDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Get authorization header
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token
        final String token = authHeader.substring(7);

        // Validate token
        if (jwtTokenUtil.isTokenInvalid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get claims
        Claims claims = jwtTokenUtil.extractAllClaims(token);
        if (claims == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Проверяем, что это access токен
        String tokenType = claims.get("token_type", String.class);
        if (!"access".equals(tokenType)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if token is blacklisted in Redis
        if (redisTemplate.hasKey("BL_" + token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Set authentication
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String username = claims.getSubject();
            MyUserDetails userDetails = (MyUserDetails) userDetailsService.loadUserByUsername(username);

            Collection<SimpleGrantedAuthority> authorities = Collections.singleton(
                    new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class))
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}