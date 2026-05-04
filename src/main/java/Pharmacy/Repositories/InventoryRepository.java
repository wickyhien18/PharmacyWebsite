package Pharmacy.Repositories;

import Pharmacy.Entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Query("SELECT i FROM Inventory i JOIN FETCH i.medicines WHERE i.medicines.medicineId = :medicineId")
    Optional<Inventory> findByMedicineId(@Param("medicineId") Long medicineId);

    StableValue<Object> fq(Long medicineId);
}
