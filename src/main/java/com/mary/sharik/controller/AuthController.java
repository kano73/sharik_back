package com.mary.sharik.controller;

import com.mary.sharik.config.security.jwt.JwtTokenUtil;
import com.mary.sharik.config.security.jwt.TokenStoreService;
import com.mary.sharik.model.jwt.AuthRequest;
import com.mary.sharik.model.jwt.RefreshTokenRequest;
import com.mary.sharik.service.AuthService;
import com.mary.sharik.service.MyUserDetailsService;
import com.mary.sharik.service.MyUserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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
    private final MyUserService myUserService;
    private final AuthService authService;

    public AuthController(JwtTokenUtil jwtTokenUtil, TokenStoreService tokenStoreService, MyUserDetailsService myUserDetailsService, AuthenticationManager authenticationManager, RedisTemplate<Object, Object> redisTemplate, MyUserService myUserService, AuthService authService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.tokenStoreService = tokenStoreService;
        this.myUserDetailsService = myUserDetailsService;
        this.authenticationManager = authenticationManager;
        this.redisTemplate = redisTemplate;
        this.myUserService = myUserService;
        this.authService = authService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
       return authService.updateToken(request);
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest request) {
        return authService.refreshAndAccess(request);
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
