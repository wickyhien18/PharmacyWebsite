package Pharmacy.Repositories;

import Pharmacy.Entities.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository  extends JpaRepository<Roles,Long> {

    @Query(value = "SELECT * FROM roles WHERE role_id = :id", nativeQuery = true)
    Roles findByIdDetail(@Param("id") Integer id);

    Optional<Roles> findByRoleName(String roleName);
}
