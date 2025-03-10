package com.mary.sharik.controller;

import com.mary.sharik.config.security.jwt.JwtTokenUtil;
import com.mary.sharik.config.security.jwt.TokenStoreService;
import com.mary.sharik.model.jwt.AuthResponse;
import com.mary.sharik.model.details.MyUserDetails;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.jwt.AuthRequest;
import com.mary.sharik.model.jwt.RefreshTokenRequest;
import com.mary.sharik.service.MyUserDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final TokenStoreService tokenStoreService;
    private final MyUserDetailsService myUserDetailsService;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<Object, Object> redisTemplate;

    public AuthController(JwtTokenUtil jwtTokenUtil, TokenStoreService tokenStoreService, MyUserDetailsService myUserDetailsService, AuthenticationManager authenticationManager, RedisTemplate<Object, Object> redisTemplate) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.tokenStoreService = tokenStoreService;
        this.myUserDetailsService = myUserDetailsService;
        this.authenticationManager = authenticationManager;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Проверяем, не в черном ли списке токен
        Boolean isBlacklisted = redisTemplate.hasKey("BL_" + refreshToken);
        if (isBlacklisted) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        // Проверяем валидность refresh токена
        if (!jwtTokenUtil.isTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired");
        }

        Claims claims = jwtTokenUtil.extractAllClaims(refreshToken);

        // Проверяем, что это действительно refresh токен
        String tokenType = claims.get("token_type", String.class);
        if (!"refresh".equals(tokenType)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token type");
        }

        String username = claims.getSubject();
        String userId = claims.get("id", String.class);

        // Загружаем данные пользователя для получения его роли
        MyUserDetails userDetails = (MyUserDetails) myUserDetailsService.loadUserByUsername(username);
        MyUser user = userDetails.getMyUser();

        // Генерируем новый access токен
        String newAccessToken = jwtTokenUtil.generateAccessToken(
                username,
                userId,
                user.getRole()
        );
        final String refreshedToken = jwtTokenUtil.generateRefreshToken(
                user.getUsername(),
                user.getId()
        );

        // Сохраняем новый access токен
        tokenStoreService.storeAccessToken(newAccessToken, userId);
        tokenStoreService.storeRefreshToken(refreshedToken, userId);

        return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshedToken));
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        final MyUserDetails userDetails = (MyUserDetails) myUserDetailsService.loadUserByUsername(request.getUsername());

        // Assuming User implements UserDetails and has id and role fields
        MyUser user = userDetails.getMyUser();
        final String token = jwtTokenUtil.generateAccessToken(
                user.getUsername(),
                user.getId(),
                user.getRole()
        );
        final String refreshedToken = jwtTokenUtil.generateRefreshToken(
                user.getUsername(),
                user.getId()
        );

        // Store token in Redis
        tokenStoreService.storeAccessToken(token, user.getId());
        tokenStoreService.storeRefreshToken(refreshedToken, user.getId());

        return ResponseEntity.ok(new AuthResponse(token, refreshedToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Получаем ID пользователя из токена
            Claims claims = jwtTokenUtil.extractAllClaims(token);
            if (claims != null) {
                String userId = claims.get("id", String.class);

                // Инвалидируем все токены пользователя
                tokenStoreService.invalidateAllUserTokens(userId);
                return ResponseEntity.ok("Logged out successfully");
            }
        }
        return ResponseEntity.badRequest().body("No token provided");
    }
}
