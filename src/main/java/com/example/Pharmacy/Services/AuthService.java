package com.example.Pharmacy.Services;

import com.example.Pharmacy.DTO.Request.LoginRequest;
import com.example.Pharmacy.DTO.Request.RefreshTokenRequest;
import com.example.Pharmacy.DTO.Request.RegisterRequest;
import com.example.Pharmacy.DTO.Response.AuthResponse;
import com.example.Pharmacy.Entities.RefreshToken;
import com.example.Pharmacy.Entities.Users;
import com.example.Pharmacy.Entities.Roles;
import com.example.Pharmacy.Exceptions.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


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
        user.setActive(true);
        user.setRoles(role);

        userService.insert(user);
        return issueTokenPair(user);
    }

    // ĐĂNG NHẬP
    @Transactional
    public AuthResponse login(LoginRequest req) {
        userService.findByUserName(req.getUserName())
                .ifPresent(u ->
                        refreshTokenService.deleteAllByUserId(u.getUserId()));

        try {
            // AuthenticationManager gọi UserDetailsService + PasswordEncoder nội bộ
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUserName(), req.getPassword())
            );
        } catch (DisabledException e) {
            throw new AuthException("Tài khoản đã bị khoá. Vui lòng liên hệ hỗ trợ");
        } catch (BadCredentialsException e) {
            // Không tiết lộ usernam có tồn tại không → tránh user enumeration attack
            throw new AuthException("Username hoặc mật khẩu không đúng");
        }

        Users user = userService.findByUserName(req.getUserName())
                .orElseThrow(() -> new AuthException("Tài khoản không tồn tại"));

        return issueTokenPair(user);

    }

    // LÀM MỚI TOKEN
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest refreshToken) {

        RefreshToken storedToken = refreshTokenService
                .findByToken(jwtService.hashRefreshToken(refreshToken.getRefreshToken()))
                .orElseThrow(() -> new AuthException("Token không hợp lệ"));

        Users users = storedToken.getUsers();

        refreshTokenService.extendExpiryDate(storedToken.getToken());

        return issueTokenPair(users);
    }

    @Transactional
    public void logout(RefreshTokenRequest refreshToken) {
        if (refreshToken != null && !refreshToken.getRefreshToken().isEmpty()) {
            refreshTokenService.deleteRefreshToken(jwtService.hashRefreshToken(refreshToken.getRefreshToken()));
        }
    }

    /**
     * Tạo cặp access token + refresh token, lưu hash vào DB.
     * Gọi sau: register, login, refresh.
     */
    private AuthResponse issueTokenPair(Users user) {
        String accessToken       = jwtService.generateAccessToken(user.getUserName());
        String plainRefreshToken = jwtService.generateRefreshToken();

        // Chỉ lưu HASH — plain token chỉ tồn tại trong bộ nhớ rồi trả về client
        refreshTokenService.createRefreshToken(user.getUserId(), jwtService.hashRefreshToken(plainRefreshToken));

        return new AuthResponse(
                accessToken,
                plainRefreshToken,
                604800000 / 1000,
                toUserInfo(user)
        );
    }

    /**
     * Map User entity → UserInfo DTO.
     * Dùng lại ở register, login, refresh, getCurrentUserInfo.
     */
    private AuthResponse.UserInfo toUserInfo(Users user) {
        return new AuthResponse.UserInfo(
                user.getUserId(),
                user.getUserName(),
                user.getRoles()
        );
    }


}
