package Pharmacy.Repositories;

import Pharmacy.Entities.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
/**
 * Repository interface for RoleRepository.
 * This class is used to map data and handle basic structure.
 */
public interface RoleRepository  extends JpaRepository<Roles,Long> {
    Optional<Roles> findByRoleName(String roleName);
}
