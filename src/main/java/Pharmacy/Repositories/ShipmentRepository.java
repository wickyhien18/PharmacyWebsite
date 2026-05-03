package Pharmacy.Repositories;

import Pharmacy.Entities.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    @Query("SELECT s FROM Shipment s JOIN FETCH s.orders WHERE s.orders.orderId = :orderId")
    Optional<Shipment> findByOrderId(@Param("orderId") Long orderId);

}
