package com.example.Pharmacy.Services;

import com.example.Pharmacy.DTO.LoginRequest;
import com.example.Pharmacy.DTO.RefreshTokenRequest;
import com.example.Pharmacy.DTO.RegisterRequest;
import com.example.Pharmacy.Entities.Roles;
import com.example.Pharmacy.Entities.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

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
    public String register(RegisterRequest req) {

        if (userService.existByUserName(req.getUserName()))
            return "Username đã tồn tại";

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
    public String login(LoginRequest req) {
        userService.findByUserName(req.getUserName())
                .ifPresent(u ->
                        refreshTokenService.deleteAllByUserId(u.getUserId()));

        Authentication auth;
        try {
            auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUserName(), req.getPassword()));
        } catch (Exception e) {
            return "Sai username hoặc mật khẩu";
        }

        SecurityContextHolder.getContext().setAuthentication(auth);
        Users users = userService.findByUserName(req.getUserName())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy User"));

        String refreshToken = jwtService.generateRefreshToken(
                users.getUserName());

        refreshTokenService.createRefreshToken(
                users.getUserId(), refreshToken);

        return jwtService.generateAccessToken(users.getUserName());
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
