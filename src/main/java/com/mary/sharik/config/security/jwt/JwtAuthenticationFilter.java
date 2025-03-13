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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final MyUserRepository myUserRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Extract token
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            filterChain.doFilter(request, response);
            return;
        }

        final String token = Arrays.stream(cookies).filter(item->
                item.getName().equals("accessToken"))
                .findFirst().map(Cookie::getValue).orElse(null);


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

        // Check if token is blacklisted in Redis
        if (redisTemplate.hasKey("BL_" + token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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