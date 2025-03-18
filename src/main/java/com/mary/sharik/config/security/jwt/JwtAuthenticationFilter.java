package com.mary.sharik.config.security.jwt;

import com.mary.sharik.exceptions.NoDataFoundException;
import com.mary.sharik.model.details.MyUserDetails;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.repository.MyUserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final MyUserRepository myUserRepository;

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("please login");
        response.getWriter().flush();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (Objects.equals(request.getRequestURI(), "/login") ||
                Objects.equals(request.getRequestURI(), "/register" ) ||
                Objects.equals(request.getRequestURI(), "/logout" )) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            sendErrorResponse(request, response, filterChain);
            return;
        }

        final String token = Arrays.stream(cookies).filter(item->
                item.getName().equals("accessToken"))
                .findFirst().map(Cookie::getValue).orElse(null);

        // Validate token
        if (jwtTokenUtil.isTokenInvalid(token)) {
            sendErrorResponse(request, response, filterChain);
            return;
        }

        // Get claims
        Claims claims = jwtTokenUtil.extractAllClaims(token);
        if (claims == null) {
            sendErrorResponse(request, response, filterChain);
            return;
        }

        // Check if token is blacklisted in Redis
        if (redisTemplate.hasKey("BL_" + token)) {
            sendErrorResponse(request, response, filterChain);
            return;
        }

        // Set authentication
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String userId = claims.get("id", String.class);
            MyUser user = myUserRepository.findById(userId).orElseThrow(
                    ()-> new NoDataFoundException("No user found with id " + userId)
            );

            Collection<SimpleGrantedAuthority> authorities = Collections.singleton(
                    new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class))
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(new MyUserDetails(user), null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);


        }

        filterChain.doFilter(request, response);
    }
}