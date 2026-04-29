package Pharmacy.Services;

import Pharmacy.DTO.Request.LoginRequest;
import Pharmacy.DTO.Request.RefreshTokenRequest;
import Pharmacy.DTO.Request.RegisterRequest;
import Pharmacy.DTO.Response.AuthResponse;
import Pharmacy.Entities.RefreshToken;
import Pharmacy.Entities.Users;
import Pharmacy.Entities.Roles;
import Pharmacy.Exceptions.AuthException;
import Pharmacy.Exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JWTService      jwtService;
    private final AuthenticationManager authManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {

        if (userService.existByUserName(req.getUserName()))
            throw new AuthException("UserName is already exist");

        Roles role = roleService
                .findByRoleName("CUSTOMER")
                .orElseThrow();

        Users user = new Users();
        user.setUserName(req.getUserName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setActive(true);
        user.setRoles(role);

        userService.insert(user);
        return createTokenPair(user);
    }

    // ĐĂNG NHẬP
    @Transactional
    public AuthResponse login(LoginRequest req) {

        userService.findByUserName(req.getUserName())
                .ifPresent(u ->
                        refreshTokenService.deleteAllByUserId(u.getUserId()));

        try {
            // AuthenticationManager call local  UserDetailsService + PasswordEncoder
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUserName(), req.getPassword())
            );
        } catch (DisabledException e) {
            throw new AuthException("Account has been locked");
        } catch (BadCredentialsException e) {
            throw new AuthException("UserName or Password is incorrect");
        }

        Users user = userService.findByUserName(req.getUserName())
                .orElseThrow(() -> new AuthException("Not found UserName"));

        return createTokenPair(user);

    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest refreshToken) {

        RefreshToken storedToken = refreshTokenService
                .findByToken(jwtService.hashRefreshToken(refreshToken.getRefreshToken()))
                .orElseThrow(() -> new AuthException("Invalid Token"));

        Users users = storedToken.getUsers();

        refreshTokenService.extendExpiryDate(storedToken.getToken());

        return createTokenPair(users);
    }

    @Transactional
    public String logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResourceNotFoundException("Not found Token");
        }

        String accessToken = header.substring(7);

        String username = jwtService.getUsername(accessToken);

        userService.findByUserName(username)
                .ifPresent(users -> refreshTokenService.deleteAllByUserId(users.getUserId()));

        return "Logout Success";
    }

    
    private AuthResponse createTokenPair(Users user) {
        String accessToken       = jwtService.generateAccessToken(user.getUserName());
        String plainRefreshToken = jwtService.generateRefreshToken();

        // Only add hash refresh token
        refreshTokenService.createRefreshToken(user.getUserId(), jwtService.hashRefreshToken(plainRefreshToken));

        return new AuthResponse(
                accessToken,
                plainRefreshToken,
                "1 hour",
                toUserInfo(user)
        );
    }

    private AuthResponse.UserInfo toUserInfo(Users user) {
        return new AuthResponse.UserInfo(
                user.getUserId(),
                user.getUserName(),
                user.getRoles()
        );
    }


}
