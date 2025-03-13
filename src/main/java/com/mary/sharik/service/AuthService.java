package com.mary.sharik.service;

import com.mary.sharik.config.security.jwt.JwtTokenUtil;
import com.mary.sharik.config.security.jwt.TokenStoreService;
import com.mary.sharik.model.dto.storage.ProductAndQuantity;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.jwt.AuthRequest;
import com.mary.sharik.model.jwt.AuthResponse;
import com.mary.sharik.model.jwt.RefreshTokenRequest;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final RedisTemplate<String, ProductAndQuantity> redisTemplate;
    private final JwtTokenUtil jwtTokenUtil;
    private final MyUserService myUserService;
    private final TokenStoreService tokenStoreService;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<?> updateToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Проверяем, не в черном ли списке токен
        Boolean isBlacklisted = redisTemplate.hasKey("BL_" + refreshToken);
        if (isBlacklisted) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        // Проверяем валидность refresh токена
        if (jwtTokenUtil.isTokenInvalid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired");
        }

        Claims claims = jwtTokenUtil.extractAllClaims(refreshToken);

        // Проверяем, что это действительно refresh токен
        String tokenType = claims.get("token_type", String.class);
        if (!"refresh".equals(tokenType)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token type");
        }

        String userId = claims.get("id", String.class);

        // Загружаем данные пользователя для получения его роли
        MyUser user = myUserService.findById(userId);

        // Генерируем новый access токен
        String token = jwtTokenUtil.generateAccessToken(
                userId,
                user.getRole()
        );
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(
                user.getId()
        );

        return getResponseWithTokens(token, newRefreshToken);
    }

    public ResponseEntity<?> refreshAndAccess(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        MyUser user = myUserService.findByEmail(request.getEmail());

        String token = jwtTokenUtil.generateAccessToken(
                user.getId(),
                user.getRole()
        );
        String refreshToken = jwtTokenUtil.generateRefreshToken(
                user.getId()
        );

        return getResponseWithTokens(token, refreshToken);
    }

    private ResponseEntity<?> getResponseWithTokens(String token, String newRefreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .path("/")
                .maxAge(3600) // 10 час
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(7 * 24 * 3600) // 7 дней
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("Login successful");
    }

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
