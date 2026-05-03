package Pharmacy.Controllers;

import Pharmacy.DTO.Request.LoginRequest;
import Pharmacy.DTO.Request.RefreshTokenRequest;
import Pharmacy.DTO.Request.RegisterRequest;
import Pharmacy.DTO.Response.ApiResponse;
import Pharmacy.DTO.Response.AuthResponse;
import Pharmacy.DTO.Response.LoginResponse;
import Pharmacy.Entities.Users;
import Pharmacy.Repositories.UserRepository;
import Pharmacy.Services.AuthService;
import Pharmacy.Services.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import  org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    @Operation(summary = "Register Account")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest req) {

        AuthResponse data = authService.register(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Register Successfully", data));
    }

    @PostMapping("/login")
    @Operation(summary = "Login to get Jwt token")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest req) {

        AuthResponse response = authService.login(req);

        return ResponseEntity.ok(ApiResponse.ok("Login Successfully", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh jwt token")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest refreshToken) {

        AuthResponse data = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(ApiResponse.ok("Refresh Token Successfully", data));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and delete refreshToken")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        String result = authService.logout(request);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/me")
    @Operation(summary = "Account info")
    public ResponseEntity<?> me() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(ApiResponse.fail("Unauthorized"));
        }

        String email = authentication.getName();

        System.out.println("Email: " + email);

        Users currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var info = new AuthResponse.UserInfo(
                currentUser.getUserId(),
                currentUser.getUsername(),
                currentUser.getFullName(),
                currentUser.getEmail(),
                currentUser.getPhone(),
                currentUser.getRoles() != null ? currentUser.getRoles().getRoleName() : ""
        );
        return ResponseEntity.ok(ApiResponse.ok(info));
    }
}
