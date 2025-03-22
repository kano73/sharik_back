package com.mary.sharik.service;

import com.mary.sharik.config.security.jwt.JwtTokenUtil;
import com.mary.sharik.config.security.jwt.TokenStoreService;
import com.mary.sharik.model.dto.storage.ProductAndQuantity;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enums.TokenType;
import com.mary.sharik.model.jwt.AuthRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Getter
public class AuthService {
    private final RedisTemplate<String, ProductAndQuantity> redisTemplate;
    private final JwtTokenUtil jwtTokenUtil;
    private final MyUserService myUserService;
    private final TokenStoreService tokenStoreService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

//      10 min
    public static final Integer MAX_AGE_ACCESS = 360;
//      7 days
    public static final Integer MAX_AGE_REFRESH = 604800;

    public ResponseEntity<?> refreshAndAccessForCurrentUser(AuthRequest request) {
        MyUser user = myUserService.findByEmail(request.getEmail());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        return generateTokensWithId(user.getId());
    }

    public ResponseEntity<?> generateTokensWithId(String id) {
        String token = jwtTokenUtil.generateAccessToken(
                id
        );
        String refreshToken = jwtTokenUtil.generateRefreshToken(
                id
        );

        return getResponseWithTokens(token, refreshToken);
    }

    public static ResponseCookie tokenToCookie(String token, TokenType type, Integer age) {
        return ResponseCookie.from(type.toString(), token)
                .httpOnly(true)
                .path("/")
                .maxAge(age) // 1 час
                .sameSite("Strict")
                .build();
    }

    private ResponseEntity<?> getResponseWithTokens(String token, String newRefreshToken) {
        ResponseCookie accessCookie = tokenToCookie(token, TokenType.accessToken, MAX_AGE_ACCESS);

        ResponseCookie refreshCookie = tokenToCookie(newRefreshToken, TokenType.refreshToken, MAX_AGE_REFRESH);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("Login successful");
    }

    public ResponseEntity<?> logout() {
        return getResponseWithTokens("", "");
    }
}
