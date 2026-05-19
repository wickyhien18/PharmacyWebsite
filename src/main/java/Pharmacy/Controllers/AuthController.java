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


/**
 * REST Controller for managing authentication-related operations.
 * This class handles user registration, login, token refresh, logout,
 * and retrieving current user information.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API")
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account in the system.
     *
     * @param req The registration request containing user details (username, password, email, etc.).
     * @return ResponseEntity containing a success message and the newly generated authentication tokens.
     */
    @PostMapping("/register")
    @Operation(summary = "Register Account")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest req) {

        AuthResponse data = authService.register(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Register Successfully", data));
    }

    /**
     * Authenticates a user and generates a JWT access token and a refresh token.
     *
     * @param req The login request containing user credentials (email and password).
     * @return ResponseEntity containing a success message, the access token, refresh token, and user info.
     */
    @PostMapping("/login")
    @Operation(summary = "Login to get Jwt token")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest req) {

        AuthResponse response = authService.login(req);

        return ResponseEntity.ok(ApiResponse.ok("Login Successfully", response));
    }

    /**
     * Generates a new access token using a valid refresh token.
     *
     * @param refreshToken The refresh token request containing the valid refresh token string.
     * @return ResponseEntity containing the new access token and refresh token pair.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh jwt token")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest refreshToken) {

        AuthResponse data = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(ApiResponse.ok("Refresh Token Successfully", data));
    }

    /**
     * Logs out the currently authenticated user by invalidating their refresh token.
     *
     * @param request The HTTP request containing the Authorization header with the Bearer token.
     * @return ResponseEntity containing a success message indicating the user was logged out.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout and delete refreshToken")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        String result = authService.logout(request);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Retrieves the profile information of the currently authenticated user.
     *
     * @param request The HTTP request containing the Authorization header with the Bearer token.
     * @return ResponseEntity containing the user's detailed profile information.
     */
    @GetMapping("/me")
    @Operation(summary = "Account info")
    public ResponseEntity<?> me(HttpServletRequest request) {

        var info = authService.me(request);

        return ResponseEntity.ok(ApiResponse.ok(info));
    }
}
