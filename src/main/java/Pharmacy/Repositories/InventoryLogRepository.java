package Pharmacy.Repositories;

import Pharmacy.Entities.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {
    @Query("SELECT il FROM InventoryLog il JOIN FETCH il.medicines WHERE il.medicines.medicineId = :medicineId ORDER BY il.createdAt DESC")
    List<InventoryLog> findByMedicineIdOrderByCreatedAtDesc(@Param("medicineId") Long medicineId);
}
