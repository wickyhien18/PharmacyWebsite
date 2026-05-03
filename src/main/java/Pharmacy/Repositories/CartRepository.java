package Pharmacy.Repositories;

import Pharmacy.Entities.Carts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Carts,Long> {
    @Query("SELECT c FROM Carts c JOIN FETCH c.users where c.users.userId = :userId")
    Optional<Carts> findByUserId(@Param("userId") Long userId);
}
