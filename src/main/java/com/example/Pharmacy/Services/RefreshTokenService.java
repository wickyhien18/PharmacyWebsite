package com.example.Pharmacy.Services;

import com.example.Pharmacy.Entities.RefreshToken;
import com.example.Pharmacy.Entities.Users;
import com.example.Pharmacy.Repositories.RefreshTokenRepository;
import com.example.Pharmacy.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTService jwtService;

    // Tạo refresh token mới
    @Transactional
    public RefreshToken createRefreshToken(Integer userId, String token) {

        Users user = userRepository.getReferenceById(Long.valueOf(userId));

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .users(user)
                .expire_at(LocalDateTime.now().plusDays(7)) // 7 ngày
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean validateRefreshToken(String token) {
        Optional<RefreshToken> storedToken = refreshTokenRepository.findByToken(token);

        if (storedToken.isEmpty()) {
            return false;
        }

        RefreshToken rt = storedToken.get();

        // Kiểm tra hết hạn
        if (rt.isExpired()) {
            return false;
        }

        // Kiểm tra JWT
        return jwtService.validateRefreshToken(token);
    }

    // Xóa refresh token
    @Transactional
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    // Xóa tất cả token của user
    @Transactional
    public void deleteAllByUserId(Integer userId) {
        refreshTokenRepository.deleteAllByUsers_UserId(userId);
    }

    // Lấy user từ refresh token
    public Optional<Users> getUserByRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(RefreshToken::getUsers);
    }

    // Gia hạn refresh token
    @Transactional
    public boolean extendExpiryDate(String token) {
        Optional<RefreshToken> optional = refreshTokenRepository.findByToken(token);
        if (optional.isEmpty()) {
            return false;
        }

        RefreshToken rt = optional.get();
        rt.setExpire_at(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(rt);
        return true;
    }
}
