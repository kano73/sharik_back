package com.mary.sharik.controller;

import com.mary.sharik.model.jwt.AuthRequest;
import com.mary.sharik.model.jwt.RefreshTokenRequest;
import com.mary.sharik.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
       return authService.updateToken(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest request) {
        return authService.refreshAndAccess(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        System.out.println("Logout called");
        return authService.logout();
    }
}
