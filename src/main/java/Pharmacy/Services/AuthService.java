package Pharmacy.Services;

import Pharmacy.Config.JWTUtil;
import Pharmacy.DTO.Request.LoginRequest;
import Pharmacy.DTO.Request.RefreshTokenRequest;
import Pharmacy.DTO.Request.RegisterRequest;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


/**
 * Service class for handling authentication and authorization logic.
 * Manages user registration, login, token refresh, and user session operations.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtService;
    private final AuthenticationManager authManager;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Registers a new user account with default CUSTOMER role.
     *
     * @param req The registration request containing username, password, email, etc.
     * @return AuthResponse containing the access and refresh tokens.
     * @throws AuthException if email, phone, or username already exists.
     */
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

    /**
     * Authenticates a user based on email and password.
     *
     * @param req The login request containing email and password.
     * @return AuthResponse containing the new access and refresh tokens.
     * @throws AuthException if credentials are bad or account is locked.
     */
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


    /**
     * Refreshes the access token using a valid, unexpired refresh token.
     *
     * @param refreshToken The request containing the refresh token string.
     * @return AuthResponse containing a new token pair.
     * @throws AuthException if the token is invalid or expired.
     */
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

    /**
     * Logs out the user by deleting all their active refresh tokens.
     *
     * @param request The HttpServletRequest to extract the Authorization token from.
     * @return A success message.
     * @throws ResourceNotFoundException if the token is missing.
     */
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

    /**
     * Retrieves information about the currently logged-in user.
     *
     * @param request The HttpServletRequest containing the Authorization header.
     * @return UserInfo object containing current user's profile details.
     * @throws ResourceNotFoundException if the token or user cannot be found.
     */
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
    
    /**
     * Helper method to create and save a new access/refresh token pair for a user.
     * This will invalidate previous tokens by deleting them.
     *
     * @param user The user to generate tokens for.
     * @return AuthResponse containing the token pair and user info.
     */
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
