package com.example.Pharmacy.Controllers;

import com.example.Pharmacy.DTO.LoginRequest;
import com.example.Pharmacy.DTO.RefreshRequest;
import com.example.Pharmacy.DTO.RegisterRequest;
import com.example.Pharmacy.Services.AuthService;
import com.example.Pharmacy.Services.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "Đăng nhập và đăng xuất qua JWT")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản (vai trò khách hàng)")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập vào để nhận jwt token")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest req) {
        String token = authService.login(req);

        if (token.startsWith("Sai"))
            return ResponseEntity.status(401).body(token);

        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới jwt token")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest refreshToken) {

        if (refreshToken == null || refreshToken.getRefreshToken().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refreshToken là bắt buộc"));
        }

        if (!refreshTokenService.validateRefreshToken(refreshToken.getRefreshToken())) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token không hợp lệ hoặc đã hết hạn"));
        }

        return ResponseEntity.ok(authService.refreshToken(refreshToken.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất và xoá refreshToken")
    public ResponseEntity<?> logout(@RequestBody RefreshRequest refreshToken) {

        if (refreshToken != null && !refreshToken.getRefreshToken().isEmpty()) {
            refreshTokenService.deleteRefreshToken(refreshToken.getRefreshToken());
        }

        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

}
