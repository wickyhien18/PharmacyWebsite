package Pharmacy.Repositories;

import Pharmacy.Entities.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Indicates that this class is a Data Access Object (DAO) interfacing with the database.
@Repository
/**
 * Repository interface for CartItemRepository.
 * This class is used to map data and handle basic structure.
 */
public interface CartItemRepository extends JpaRepository<CartItems, Long> {
    @Query("SELECT ci FROM CartItems ci JOIN FETCH ci.carts JOIN FETCH ci.medicines WHERE ci.carts.cartId = :cartId AND ci.medicines.medicineId = :medicineId")
    Optional<CartItems> findByCartIdAndMedicineId(@Param("cartId") Long cartId,@Param("medicineId") Long medicineId);
}
