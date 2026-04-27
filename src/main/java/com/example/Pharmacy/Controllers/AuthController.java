package com.example.Pharmacy.Controllers;

import com.example.Pharmacy.DTO.Request.LoginRequest;
import com.example.Pharmacy.DTO.Request.RefreshTokenRequest;
import com.example.Pharmacy.DTO.Request.RegisterRequest;
import com.example.Pharmacy.DTO.Response.ApiResponse;
import com.example.Pharmacy.DTO.Response.AuthResponse;
import com.example.Pharmacy.DTO.Response.LoginResponse;
import com.example.Pharmacy.Repositories.UserRepository;
import com.example.Pharmacy.Services.AuthService;
import com.example.Pharmacy.Services.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
            @Valid @RequestBody RegisterRequest req) {

        AuthResponse data = authService.register(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đăng ký thành công", data));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập vào để nhận jwt token")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest req) {

        AuthResponse response = authService.login(req);

        return ResponseEntity.ok(ApiResponse.ok("Đăng nhập thành công", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới jwt token")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest refreshToken) {

        AuthResponse data = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(ApiResponse.ok("Làm mới token thành công", data));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất và xoá refreshToken")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        String result = authService.logout(request);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

}
