package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository  extends JpaRepository<Roles,Long> {

    @Query(value = "SELECT * FROM roles WHERE role_id = :id", nativeQuery = true)
    Roles findByIdDetail(@Param("id") Integer id);
}
