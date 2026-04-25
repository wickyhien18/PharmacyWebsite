package com.example.Pharmacy.Repositories;


import com.example.Pharmacy.Entities.RefreshToken;
import com.example.Pharmacy.Entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    @Modifying
    @Transactional
    void deleteAllByUsers_UserId(Integer userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expire_at < :now")
    void deleteAllExpiredTokens(@Param("now") LocalDateTime now);
}
