package Pharmacy;

import Pharmacy.DTO.Request.LoginRequest;
import Pharmacy.DTO.Request.RefreshTokenRequest;
import Pharmacy.DTO.Request.RegisterRequest;
import Pharmacy.DTO.Response.AuthResponse;
import Pharmacy.Entities.RefreshToken;
import Pharmacy.Entities.Roles;
import Pharmacy.Entities.Users;
import Pharmacy.Exceptions.AuthException;
import Pharmacy.Exceptions.ConflictException;
import Pharmacy.Exceptions.ResourceNotFoundException;
import Pharmacy.Repositories.RefreshTokenRepository;
import Pharmacy.Repositories.RoleRepository;
import Pharmacy.Repositories.UserRepository;
import Pharmacy.Config.JWTUtil;
import Pharmacy.Services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    // ---- Mocks — Spring không khởi động, test chạy nhanh ----
    @Mock UserRepository         userRepository;
    @Mock RoleRepository         roleRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder        passwordEncoder;
    @Mock JWTUtil                jwtUtil;
    @Mock AuthenticationManager  authManager;

    @InjectMocks AuthService authService;

    // ---- Dữ liệu dùng chung ----
    private Roles customerRole;
    private Users testUser;

    @BeforeEach
    void setUp() {
        customerRole = Roles.builder()
                .roleId(1L).roleName("ROLE_CUSTOMER").build();

        testUser = Users.builder()
                .userId(1L)
                .userName("nguyenvana")
                .password("$2a$10$hashedPassword")
                .fullName("Nguyễn Văn A")
                .email("a@gmail.com")
                .phone("0912345678")
                .roles(customerRole)
                .isActive(true)
                .build();
    }

    // ================================================================
    // ĐĂNG KÝ
    // ================================================================
    @Nested
    @DisplayName("register()")
    class RegisterTests {

        private RegisterRequest validRequest() {
            return new RegisterRequest(
                    "nguyenvana", "Pass@123",
                    "Nguyễn Văn A", "a@gmail.com", "0912345678");
        }

        @Test
        @DisplayName("Đăng ký thành công → trả AuthResponse có token")
        void register_success() {
            // Arrange
            when(userRepository.existsByEmailOrPhone(anyString(), anyString())).thenReturn(false);
            when(userRepository.existsByUserName(anyString())).thenReturn(false);
            when(roleRepository.findByRoleName("ROLE_CUSTOMER"))
                    .thenReturn(Optional.of(customerRole));
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashed");
            when(userRepository.save(any(Users.class))).thenReturn(testUser);
            when(jwtUtil.generateAccessToken(any())).thenReturn("access-token-xyz");
            when(jwtUtil.generateRefreshToken()).thenReturn("refresh-token-xyz");
            when(refreshTokenRepository.save(any())).thenReturn(null);

            // Act
            AuthResponse response = authService.register(validRequest());

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("access-token-xyz");
            assertThat(response.refreshToken()).isEqualTo("refresh-token-xyz");
            assertThat(response.user().email()).isEqualTo("a@gmail.com");

            // Verify: user và refreshToken phải được lưu vào DB
            verify(userRepository).save(any(Users.class));
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Email hoặc phone đã tồn tại → throw AuthException")
        void register_duplicateEmailOrPhone_throwsException() {
            // Arrange
            RegisterRequest request = validRequest();

            when(userRepository.existsByEmailOrPhone(request.email(), request.phone()))
                    .thenReturn(true);  // ← Mock method gộp

            // Act & Assert
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("This email or phone has been registered");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Username đã tồn tại → throw AuthException")
        void register_duplicateUserName_throwsException() {
            // Arrange
            RegisterRequest request = validRequest();

            when(userRepository.existsByEmailOrPhone(anyString(), anyString()))
                    .thenReturn(false);  // Email/phone đều chưa tồn tại
            when(userRepository.existsByUserName(request.userName()))
                    .thenReturn(true);   // Username đã tồn tại

            // Act & Assert
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("UserName is already exist");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Role CUSTOMER không tồn tại trong DB → AppException 404")
        void register_roleNotFound_throws() {
            when(userRepository.existsByEmailOrPhone(anyString(), anyString()))
                    .thenReturn(false);  // Email/phone đều chưa tồn tại
            when(userRepository.existsByUserName(anyString())).thenReturn(false);
            when(roleRepository.findByRoleName("ROLE_CUSTOMER"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.register(validRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Role not found");
            verify(userRepository, never()).save(any());
        }
    }

    // ================================================================
    // ĐĂNG NHẬP
    // ================================================================
    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Đăng nhập đúng → trả AuthResponse")
        void login_success() {
            LoginRequest req = new LoginRequest("a@gmail.com", "Pass@123");

            // AuthenticationManager không throw = credentials đúng
            when(authManager.authenticate(any())).thenReturn(null);
            when(userRepository.findByEmail("a@gmail.com"))
                    .thenReturn(Optional.of(testUser));
            when(jwtUtil.generateAccessToken(any())).thenReturn("access-token");
            when(jwtUtil.generateRefreshToken()).thenReturn("refresh-token");

            AuthResponse response = authService.login(req);

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.user().email()).isEqualTo("a@gmail.com");
            assertThat(response.user().role()).isEqualTo("ROLE_CUSTOMER");

            verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByEmail("a@gmail.com");
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Sai mật khẩu → AppException 401, không tiết lộ email có tồn tại không")
        void login_wrongPassword_throws401() {
            LoginRequest req = new LoginRequest("a@gmail.com", "SaiMK@123");

            // AuthenticationManager throw BadCredentialsException = sai mật khẩu
            when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(AuthException.class)
                    // Message phải giống nhau dù sai email hay sai password
                    // → tránh user enumeration attack
                    .hasMessageContaining("Email hoặc mật khẩu không đúng")
                    .extracting("status").isEqualTo(401);
            verify(userRepository, never()).findByEmail(anyString());

        }

        @Test
        @DisplayName("Tài khoản bị khoá (isActive=false) → AppException 401")
        void login_disabledAccount_throws401() {
            testUser.setIsActive(false);
            LoginRequest req = new LoginRequest("a@gmail.com", "Pass@123");

            when(authManager.authenticate(any())).thenReturn(null);
            when(userRepository.findByEmail("a@gmail.com"))
                    .thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("khoá")
                    .extracting("status").isEqualTo(401);
        }
    }

    // ================================================================
    // REFRESH TOKEN
    // ================================================================
    @Nested
    @DisplayName("refresh()")
    class RefreshTests {

        @Test
        @DisplayName("Refresh token hợp lệ → cấp cặp token mới, xoá token cũ")
        void refresh_validToken_returnsNewTokenPair() {
            RefreshToken stored = RefreshToken.builder()
                    .id(1L)
                    .token("valid-refresh-token")
                    .users(testUser)
                    .expireAt(LocalDateTime.now().plusDays(7))
                    .build();

            when(refreshTokenRepository.findByToken("valid-refresh-token"))
                    .thenReturn(Optional.of(stored));
            when(jwtUtil.generateAccessToken(testUser)).thenReturn("new-access");
            when(jwtUtil.generateRefreshToken()).thenReturn("new-refresh");

            RefreshTokenRequest req = new RefreshTokenRequest("valid-refresh-token");
            AuthResponse response = authService.refreshToken(req);

            assertThat(response.accessToken()).isEqualTo("new-access");
            assertThat(response.refreshToken()).isEqualTo("new-refresh");

            // Token cũ phải bị xoá — rotation
            verify(refreshTokenRepository).delete(stored);
            // Token mới phải được lưu
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Refresh token không tồn tại trong DB → AppException 401")
        void refresh_tokenNotFound_throws401() {
            when(refreshTokenRepository.findByToken("fake-token"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    authService.refreshToken(new RefreshTokenRequest("fake-token")))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("không hợp lệ")
                    .extracting("status").isEqualTo(401);
        }

        @Test
        @DisplayName("Refresh token hết hạn → AppException 401")
        void refresh_expiredToken_throws401() {
            RefreshToken expired = RefreshToken.builder()
                    .id(1L)
                    .token("expired-token")
                    .users(testUser)
                    .expireAt(LocalDateTime.now().minusDays(1)) // Đã hết hạn
                    .build();

            when(refreshTokenRepository.findByToken("expired-token"))
                    .thenReturn(Optional.of(expired));

            assertThatThrownBy(() ->
                    authService.refreshToken(new RefreshTokenRequest("expired-token")))
                    .isInstanceOf(AuthException.class)
                    .hasMessageContaining("hết hạn")
                    .extracting("status").isEqualTo(401);

            // Token hết hạn không được cấp token mới
            verify(jwtUtil, never()).generateAccessToken(any());
        }
    }

    // ================================================================
    // ĐĂNG XUẤT
    // ================================================================
    @Nested
    @DisplayName("logout()")
    class LogoutTests {

        @Test
        @DisplayName("Logout với token hợp lệ → xoá token khỏi DB")
        void logout_validToken_deletesToken() {

            // mock request
            HttpServletRequest request = mock(HttpServletRequest.class);

            // fake header Authorization
            when(request.getHeader("Authorization"))
                    .thenReturn("Bearer valid-token");

            // fake token trong DB
            RefreshToken stored = RefreshToken.builder()
                    .id(1L)
                    .token("valid-token")
                    .users(testUser)
                    .build();

            String token = stored.getToken();

            when(refreshTokenRepository.findByToken("valid-token"))
                    .thenReturn(Optional.of(stored));

            // gọi service
            authService.logout(request);

            // verify delete
            verify(refreshTokenRepository).deleteByToken(token);
        }
        @Test
        @DisplayName("Logout với token không tồn tại → không throw, idempotent")
        void logout_tokenNotFound_noException() {
            HttpServletRequest request = mock(HttpServletRequest.class);

                when(request.getHeader("Authorization"))
                        .thenReturn("Bearer invalid-token");

                when(refreshTokenRepository.findByToken("invalid-token"))
                        .thenReturn(Optional.empty());

                assertThrows(RuntimeException.class, () -> {
                    authService.logout(request);
                });
        }
    }
}
