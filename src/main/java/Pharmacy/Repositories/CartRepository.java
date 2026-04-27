package Pharmacy.Repositories;

import Pharmacy.Entities.Carts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartRepository extends JpaRepository<Carts,Long> {
    @Query(value = "SELECT * FROM carts WHERE cart_id = :id", nativeQuery = true)
    Carts findByIdDetail(@Param("id") Integer id);

    @Query("SELECT c,ci,u from Carts c join c.cartItems ci join c.users u")
    List<Carts> getAll();
}
