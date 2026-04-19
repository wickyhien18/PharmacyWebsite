package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users,Long> {
    Optional<Users> findByUserName(String username);
    boolean existsByUser_name(String username);
}
