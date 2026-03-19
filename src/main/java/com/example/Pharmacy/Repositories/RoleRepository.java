package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository  extends JpaRepository<Roles,Long> {
}
