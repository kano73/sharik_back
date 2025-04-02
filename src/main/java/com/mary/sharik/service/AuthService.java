package com.mary.sharik.service;

import com.mary.sharik.config.security.jwt.JwtTokenUtil;
import com.mary.sharik.exception.ValidationFailedException;
import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enumClass.TokenType;
import com.mary.sharik.model.jwt.AuthRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
@Getter
public class AuthService {
    private final JwtTokenUtil jwtTokenUtil;
    private final MyUserService myUserService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Value("${max.age.access}")
    private Duration expirationTimeAccess;

    @Value("${max.age.refresh}")
    private Duration expirationTimeRefresh;

    public ResponseEntity<?> loginProcess(AuthRequest request) {
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

    public ResponseCookie tokenToCookie(String token, TokenType type) {
        if(type==TokenType.accessToken){
            return ResponseCookie.from(type.toString(), token)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(expirationTimeAccess)
                    .sameSite("Strict")
                    .build();
        }else if(type==TokenType.refreshToken){
            return ResponseCookie.from(type.toString(), token)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(expirationTimeRefresh)
                    .sameSite("Strict")
                    .build();
        }else{
            throw new ValidationFailedException("Unknown type");
        }
    }

    private ResponseEntity<?> getResponseWithTokens(String token, String newRefreshToken) {
        ResponseCookie accessCookie = tokenToCookie(token, TokenType.accessToken);

        ResponseCookie refreshCookie = tokenToCookie(newRefreshToken, TokenType.refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("Login successful");
    }

    public ResponseEntity<?> logout() {

        ResponseCookie accessCookie = tokenToCookie("", TokenType.accessToken);
        ResponseCookie refreshCookie = tokenToCookie("", TokenType.refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("Logged out");
    }
}
