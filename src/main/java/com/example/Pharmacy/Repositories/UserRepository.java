package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users,Long> {
}
