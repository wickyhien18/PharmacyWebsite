package Pharmacy.Services;

import Pharmacy.DTO.Request.LoginRequest;
import Pharmacy.DTO.Request.RefreshTokenRequest;
import Pharmacy.DTO.Request.RegisterRequest;
import Pharmacy.DTO.Response.ApiResponse;
import Pharmacy.DTO.Response.AuthResponse;
import Pharmacy.Entities.RefreshToken;
import Pharmacy.Entities.Users;
import Pharmacy.Entities.Roles;
import Pharmacy.Exceptions.AuthException;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.RefreshTokenRepository;
import Pharmacy.Repositories.RoleRepository;
import Pharmacy.Repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService      jwtService;
    private final AuthenticationManager authManager;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthResponse register(RegisterRequest req) {

        if (userRepository.existsByEmailOrPhone(req.email(), req.phone()))
            throw new AuthException("This email or phone has been registered");


        if (userRepository.existsByUserName(req.userName()))
            throw new AuthException("UserName is already exist");

        Roles role = roleRepository
                .findByRoleName("ROLE_CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        Users users = Users.builder()
                .userName(req.userName())
                .password(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .email(req.email())
                .phone(req.phone())
                .roles(role)
                .isActive(true)
                .build();

        userRepository.save(users);
        return createTokenPair(users);
    }

    // ĐĂNG NHẬP
    @Transactional
    public AuthResponse login(LoginRequest req) {

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
        } catch (BadCredentialsException e) {
            // Không tiết lộ email có tồn tại không
            throw new AuthException("Email or password is incorrect");
        }

        Users user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!user.isEnabled())
            throw new AuthException("Account has been locked");

        return createTokenPair(user);

    }


    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest refreshToken) {

        RefreshToken storedToken = refreshTokenRepository
                .findByToken(refreshToken.refreshToken())
                .orElseThrow(() -> new AuthException("Invalid Token"));

        if (storedToken.isExpired()) {
            throw new AuthException("Refresh Token is expired. Please login again");
        }

        Users users = storedToken.getUsers();

        refreshTokenRepository.delete(storedToken);

        return createTokenPair(users);
    }

    @Transactional
    public String logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResourceNotFoundException("Not found Token");
        }

        String accessToken = header.substring(7);

        String username = jwtService.extractEmail(accessToken);

        userRepository.findByEmail(username)
                .ifPresent(users -> refreshTokenRepository.deleteAllByUserId(users.getUserId()));

        return "Logout Successfully";
    }

    @Transactional
    public AuthResponse.UserInfo me(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResourceNotFoundException("Not found Token");
        }

        String accessToken = header.substring(7);

        String email = jwtService.extractEmail(accessToken);

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
        return info;
    }
    
    private AuthResponse createTokenPair(Users user) {

        refreshTokenRepository.deleteAllByUserId(user.getUserId());

        String accessToken       = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken();

        RefreshToken tokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .users(user)
                .expireAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(tokenEntity);

        return new AuthResponse(
                accessToken,
                refreshToken,
                3600L,
                new AuthResponse.UserInfo(
                        user.getUserId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRoles() != null ? user.getRoles().getRoleName() : ""
                )
        );
    }

}
