package Pharmacy.Repositories;


import Pharmacy.Entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

// Indicates that this class is a Data Access Object (DAO) interfacing with the database.
@Repository
/**
 * Repository interface for RefreshTokenRepository.
 * This class is used to map data and handle basic structure.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    // Defines transaction boundaries for this method/class.
    @Transactional
    void deleteByToken(String token);

    @Modifying
    // Defines transaction boundaries for this method/class.
    @Transactional
    @Query("DELETE FROM RefreshToken rt " +
            "WHERE rt.users.userId = :userId")
    void deleteAllByUserId(Long userId);

    @Modifying
    // Defines transaction boundaries for this method/class.
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expireAt < :now")
    void deleteAllExpiredTokens(@Param("now") LocalDateTime now);
}
