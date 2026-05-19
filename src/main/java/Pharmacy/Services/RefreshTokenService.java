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

// Indicates that this class provides business logic and acts as a service.
@Service
// Generates a constructor with required arguments (e.g., final fields) via Lombok.
@RequiredArgsConstructor
/**
 * Class RefreshTokenService.
 * Provides functionality and data modeling for RefreshTokenService.
 */
public class RefreshTokenService {

    // Injects the required dependency automatically via Spring DI.
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    /**
     * Finds by token.
     *
     * @param token the token
     * @return the Optional<RefreshToken> result
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // Defines transaction boundaries for this method/class.
    @Transactional
    /**
     * Deletes refresh token.
     *
     * @param token the token
     */
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    // Get user from refresh token
    /**
     * Retrieves user by refresh token.
     *
     * @param token the token
     * @return the Optional<Users> result
     */
    public Optional<Users> getUserByRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(RefreshToken::getUsers);
    }

    // Defines transaction boundaries for this method/class.
    @Transactional
    /**
     * Extend expiry date.
     *
     * @param token the token
     * @return the boolean result
     */
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
