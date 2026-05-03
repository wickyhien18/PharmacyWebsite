package Pharmacy.Repositories;

import Pharmacy.Entities.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItems, Long> {

    Optional<CartItems> findByCartCartIdAndMedicineMedicineId(Long cartId, Long medicineId);
}
