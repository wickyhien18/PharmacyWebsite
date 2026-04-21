package com.example.Pharmacy.Controllers;

import com.example.Pharmacy.DTO.LoginRequest;
import com.example.Pharmacy.DTO.RegisterRequest;
import com.example.Pharmacy.Services.AuthService;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest req) {
        String token = authService.login(req);

        if (token.startsWith("Sai"))
            return ResponseEntity.status(401).body(token);

        return ResponseEntity.ok(token);
    }
}
