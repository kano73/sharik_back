package com.mary.sharik.config.security.jwt;

import com.mary.sharik.exception.NoDataFoundException;
import com.mary.sharik.model.details.MyUserDetails;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enumClass.TokenType;
import com.mary.sharik.repository.MyUserRepository;
import com.mary.sharik.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
@Component
public class JwtTokenRefreshFilter extends OncePerRequestFilter {

    private final MyUserRepository myUserRepository;
    private final AuthService authService;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Extract token
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = Arrays.stream(cookies).filter(item -> item.getName().equals("accessToken"))
                    .findFirst().map(Cookie::getValue).orElse(null);

        if (jwtTokenUtil.isTokenExpired(token)) {
            final String refreshToken = Arrays.stream(cookies).filter(item -> item.getName()
                    .equals("refreshToken")).findFirst().map(Cookie::getValue).orElse(null);

            if (jwtTokenUtil.isTokenExpired(refreshToken)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Get claims
            Claims claims = jwtTokenUtil.extractAllClaims(refreshToken);
            if (claims == null) {
                filterChain.doFilter(request, response);
                return;
            }

            String userId = claims.get("id", String.class);

            ResponseCookie accessCookie = authService.tokenToCookie
                    (jwtTokenUtil.generateAccessToken(userId), TokenType.accessToken);
            ResponseCookie refreshCookie = authService.tokenToCookie
                    (jwtTokenUtil.generateRefreshToken(userId), TokenType.refreshToken);

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }

        // Get claims
        Claims claims = jwtTokenUtil.extractAllClaims(token);
        if (claims == null) {
            return;
        }

        // Set authentication
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String userId = claims.get("id", String.class);
            MyUser user = myUserRepository.findById(userId).orElseThrow(() ->
                    new NoDataFoundException("No user found with id " + userId));

            String role = user.getRole().name();

            Collection<SimpleGrantedAuthority> authorities = Collections.singleton
                    (new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(new MyUserDetails(user), null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}