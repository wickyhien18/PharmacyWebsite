package com.example.Pharmacy.Controllers;

import com.example.Pharmacy.DTO.Request.LoginRequest;
import com.example.Pharmacy.DTO.Request.RefreshTokenRequest;
import com.example.Pharmacy.DTO.Request.RegisterRequest;
import com.example.Pharmacy.DTO.Response.ApiResponse;
import com.example.Pharmacy.DTO.Response.LoginResponse;
import com.example.Pharmacy.Repositories.UserRepository;
import com.example.Pharmacy.Services.AuthService;
import com.example.Pharmacy.Services.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import  org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản (vai trò khách hàng)")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.register(req)));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập vào để nhận jwt token")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest req) {
        LoginResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới jwt token")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest refreshToken) {

        if (refreshToken == null || refreshToken.getRefreshToken().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refreshToken là bắt buộc"));
        }

        if (!refreshTokenService.validateRefreshToken(refreshToken.getRefreshToken())) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token không hợp lệ hoặc đã hết hạn"));
        }

        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất và xoá refreshToken")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest refreshToken) {

        if (refreshToken != null && !refreshToken.getRefreshToken().isEmpty()) {
            refreshTokenService.deleteRefreshToken(refreshToken.getRefreshToken());
        }

        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

}
