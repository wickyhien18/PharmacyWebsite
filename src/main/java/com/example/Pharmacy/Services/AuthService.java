package com.example.Pharmacy.Services;

import com.example.Pharmacy.DTO.Request.LoginRequest;
import com.example.Pharmacy.DTO.Request.RefreshTokenRequest;
import com.example.Pharmacy.DTO.Request.RegisterRequest;
import com.example.Pharmacy.DTO.Response.AuthResponse;
import com.example.Pharmacy.Entities.RefreshToken;
import com.example.Pharmacy.Entities.Users;
import com.example.Pharmacy.Entities.Roles;
import javax.naming.AuthenticationException;
import com.example.Pharmacy.Repositories.RefreshTokenRepository;
import com.example.Pharmacy.Repositories.UserRepository;
import com.example.Pharmacy.Services.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.AuthException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JWTService      jwtService;
    private final AuthenticationManager authManager;
    private final RefreshTokenService refreshTokenService;

    // ĐĂNG KÝ
    @Transactional
    public AuthResponse register(RegisterRequest req) {

        if (userService.existByUserName(req.getUserName()))
            throw new AuthException("Tên đăng nhập đã tồn tại");

        Roles role = roleService
                .findByRoleName("CUSTOMER")
                .orElseThrow();

        Users user = new Users();
        user.setUserName(req.getUserName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRoles(role);

        userService.insert(user);
        return "Đăng ký thành công";
    }

    // ĐĂNG NHẬP
    public LoginResponse login(LoginRequest req) {
        userService.findByUserName(req.getUserName())
                .ifPresent(u ->
                        refreshTokenService.deleteAllByUserId(u.getUserId()));

        Authentication auth;
        try {
            auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUserName(), req.getPassword()));
        } catch (Exception e) {
            return LoginResponse.builder()
                    .accessToken("Sai mật khẩu hoặc sai tên đăng nhập")
                    .build();
        }

        SecurityContextHolder.getContext().setAuthentication(auth);
        Users users = userService.findByUserName(req.getUserName())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy User"));

        String accessToken = jwtService.generateAccessToken(users.getUserName());
        String refreshToken = jwtService.generateRefreshToken(users.getUserName());

        refreshTokenService.createRefreshToken(
                users.getUserId(), refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // LÀM MỚI TOKEN
    public String refreshToken(RefreshTokenRequest refreshToken) {
        // Lấy username từ token
        String username = jwtService.getUsername(refreshToken.getRefreshToken());

        // Tạo access token mới
        String newAccessToken = jwtService.generateAccessToken(username);

        // Gia hạn refresh token (tùy chọn)
        refreshTokenService.extendExpiryDate(refreshToken.getRefreshToken());

        return newAccessToken;
    }


}
