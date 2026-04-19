package com.example.Pharmacy.Services;

import com.example.Pharmacy.DTO.LoginRequest;
import com.example.Pharmacy.DTO.RegisterRequest;
import com.example.Pharmacy.Entities.Roles;
import com.example.Pharmacy.Entities.Users;
import com.example.Pharmacy.Repositories.RoleRepository;
import com.example.Pharmacy.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository usersRepository;
    private final RoleRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService      jwtService;
    private final AuthenticationManager authManager;

    // ĐĂNG KÝ
    public String register(RegisterRequest req) {

        if (usersRepository.existsByUser_name(req.getUserName()))
            return "Username đã tồn tại";

        Roles role = rolesRepository
                .findByRoleName("CUSTOMER")
                .orElseThrow();

        Users user = new Users();
        user.setUsername(req.getUserName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRoles(role);

        usersRepository.save(user);
        return "Đăng ký thành công";
    }

    // ĐĂNG NHẬP
    public String login(LoginRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.getUsername(), req.getPassword()));
        } catch (Exception e) {
            return "Sai username hoặc mật khẩu";
        }

        return jwtService.generateToken(req.getUsername());
    }
}
