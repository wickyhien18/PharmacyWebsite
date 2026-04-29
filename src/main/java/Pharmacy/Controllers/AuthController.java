package Pharmacy.Controllers;

import Pharmacy.DTO.Request.LoginRequest;
import Pharmacy.DTO.Request.RefreshTokenRequest;
import Pharmacy.DTO.Request.RegisterRequest;
import Pharmacy.DTO.Response.ApiResponse;
import Pharmacy.DTO.Response.AuthResponse;
import Pharmacy.DTO.Response.LoginResponse;
import Pharmacy.Repositories.UserRepository;
import Pharmacy.Services.AuthService;
import Pharmacy.Services.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth API", description = "Đăng nhập bằng JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản (vai trò khách hàng)")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest req) {

        AuthResponse data = authService.register(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Register Successfully", data));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập vào để nhận jwt token")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest req) {

        AuthResponse response = authService.login(req);

        return ResponseEntity.ok(ApiResponse.ok("Login Successfully", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới jwt token")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest refreshToken) {

        AuthResponse data = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(ApiResponse.ok("Refresh Token Successfully", data));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất và xoá refreshToken")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        String result = authService.logout(request);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

}
