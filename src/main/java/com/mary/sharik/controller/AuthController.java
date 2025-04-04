package com.mary.sharik.controller;

import com.mary.sharik.model.jwt.AuthRequest;
import com.mary.sharik.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody @Valid AuthRequest request) {
        return authService.loginProcess(request);
    }

    @PostMapping("/auth/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody Map<String, String> body) {
        return authService.loginWithGoogleIdToken(body.get("token"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return authService.logout();
    }
}
