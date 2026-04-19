package com.example.Pharmacy.Repositories;


import com.example.Pharmacy.Entities.RefreshToken;
import com.example.Pharmacy.Entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);
    void deleteByUsers(Users users);

    Optional<RefreshToken> findByUsers(Users users);
}
