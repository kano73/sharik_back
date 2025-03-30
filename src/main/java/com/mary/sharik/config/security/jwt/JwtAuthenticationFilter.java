package com.mary.sharik.config.security.jwt;

import com.mary.sharik.exceptions.NoDataFoundException;
import com.mary.sharik.model.details.MyUserDetails;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enums.TokenType;
import com.mary.sharik.repository.MyUserRepository;
import com.mary.sharik.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_PATHS = Set.of("/login", "/register", "/logout", "/products", "/product");

    private final JwtTokenUtil jwtTokenUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final MyUserRepository myUserRepository;

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("please login");
        response.getWriter().flush();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (ALLOWED_PATHS.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            sendErrorResponse( response);
            return;
        }

        String token = Arrays.stream(cookies).filter(item->
                item.getName().equals("accessToken"))
                .findFirst().map(Cookie::getValue).orElse(null);

        if(jwtTokenUtil.isTokenExpired(token)){
            final String refreshToken = Arrays.stream(cookies).filter(item->
                            item.getName().equals("refreshToken"))
                    .findFirst().map(Cookie::getValue).orElse(null);

            if(jwtTokenUtil.isTokenExpired(refreshToken)){
                sendErrorResponse(response);
                return;
            }

            // Get claims
            Claims claims = jwtTokenUtil.extractAllClaims(refreshToken);
            if (claims == null) {
                sendErrorResponse(response);
                return;
            }

            String userId = claims.get("id", String.class);

            token = jwtTokenUtil.generateAccessToken(userId);

            ResponseCookie accessCookie = AuthService.tokenToCookie(
                    token ,
                    TokenType.accessToken,
                    AuthService.MAX_AGE_ACCESS);
            ResponseCookie refreshCookie = AuthService.tokenToCookie(
                    jwtTokenUtil.generateRefreshToken(userId),
                    TokenType.refreshToken,
                    AuthService.MAX_AGE_REFRESH);

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }

        // Validate token
        if (!jwtTokenUtil.isTokenValid(token)) {
            sendErrorResponse(response);
            return;
        }

        // Check if token is blacklisted in Redis
        if (redisTemplate.hasKey("BL_" + token)) {
            sendErrorResponse(response);
            return;
        }

        // Get claims
        Claims claims = jwtTokenUtil.extractAllClaims(token);
        if (claims == null) {
            sendErrorResponse(response);
            return;
        }

        // Set authentication
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String userId = claims.get("id", String.class);
            MyUser user = myUserRepository.findById(userId).orElseThrow(
                    ()-> new NoDataFoundException("No user found with id " + userId)
            );

            String role = user.getRole().name();

            Collection<SimpleGrantedAuthority> authorities = Collections.singleton(
                    new SimpleGrantedAuthority("ROLE_" + role)
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(new MyUserDetails(user), null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}