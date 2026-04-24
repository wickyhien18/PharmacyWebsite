package com.example.Pharmacy.Services;

import com.example.Pharmacy.DTO.LoginRequest;
import com.example.Pharmacy.DTO.RegisterRequest;
import com.example.Pharmacy.Entities.Roles;
import com.example.Pharmacy.Entities.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JWTService      jwtService;
    private final AuthenticationManager authManager;

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
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.getUserName(), req.getPassword()));
        } catch (Exception e) {
            return "Sai username hoặc mật khẩu";
        }

        return jwtService.generateToken(req.getUserName());
    }
}
