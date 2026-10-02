package com.aiops.controller;

import com.aiops.security.SecurityUtils;
import com.aiops.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        return ResponseEntity.ok(authService.login(email, password));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        return SecurityUtils.getCurrentUser()
                .map(u -> ResponseEntity.ok(Map.of(
                        "id", u.getId(),
                        "email", u.getUsername(),
                        "fullName", u.getFullName(),
                        "role", u.getRole(),
                        "tenantId", u.getTenantId()
                )))
                .orElse(ResponseEntity.status(401).build());
    }
}
