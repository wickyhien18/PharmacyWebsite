package com.example.Pharmacy.Controllers;

import com.example.Pharmacy.DTO.LoginRequest;
import com.example.Pharmacy.DTO.RegisterRequest;
import com.example.Pharmacy.Entities.Users;
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
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập vào để nhận jwt token")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest req) {
        String token = authService.login(req);

        System.out.println("=== ĐĂNG NHẬP ===");
        System.out.println("Username: " + req.getUserName());
        System.out.println("Password raw: " + req.getPassword());

        if (token.startsWith("Sai"))
            return ResponseEntity.status(401).body(token);

        // Kiểm tra user có tồn tại không
        Users user = userRepository.findByUserName(req.getUserName()).orElse(null);
        if (user == null) {
            System.out.println("❌ User không tồn tại: " + req.getUserName());
            return ResponseEntity.status(401).body("Sai tên đăng nhập hoặc mật khẩu");
        }

        System.out.println("Password in DB: " + user.getPassword());

        // Kiểm tra mật khẩu
        boolean matches = passwordEncoder.matches(req.getPassword(), user.getPassword());
        System.out.println("Password matches: " + matches);

        if (!matches) {
            System.out.println("❌ Mật khẩu không đúng");
            return ResponseEntity.status(401).body("Sai tên đăng nhập hoặc mật khẩu");
        }

        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới jwt token")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refreshToken là bắt buộc"));
        }

        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token không hợp lệ hoặc đã hết hạn"));
        }

        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất và xoá refreshToken")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.deleteRefreshToken(refreshToken);
        }

        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

}
