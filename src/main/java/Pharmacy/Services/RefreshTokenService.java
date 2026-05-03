package Pharmacy.Services;

import Pharmacy.Entities.RefreshToken;
import Pharmacy.Entities.Users;
import Pharmacy.Repositories.RefreshTokenRepository;
import Pharmacy.Repositories.UserRepository;
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

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    // Get user from refresh token
    public Optional<Users> getUserByRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(RefreshToken::getUsers);
    }

    @Transactional
    public boolean extendExpiryDate(String token) {
        Optional<RefreshToken> optional = refreshTokenRepository.findByToken(token);
        if (optional.isEmpty()) {
            return false;
        }

        RefreshToken rt = optional.get();
        rt.setExpireAt(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(rt);
        return true;
    }
}
