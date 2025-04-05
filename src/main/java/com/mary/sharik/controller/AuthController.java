package com.mary.sharik.controller;

import com.mary.sharik.model.dto.request.AuthRequest;
import com.mary.sharik.model.dto.request.GoogleAuthRequest;
import com.mary.sharik.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody @Valid AuthRequest request) {
        return authService.loginProcess(request);
    }

    @PostMapping("/auth/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody GoogleAuthRequest request) {
        return authService.loginWithGoogleIdToken(request.getToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return authService.logout();
    }
}
