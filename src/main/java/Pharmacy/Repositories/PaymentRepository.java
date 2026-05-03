package Pharmacy.Repositories;

import Pharmacy.Entities.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payments,Long> {

    @Query("SELECT p FROM Payments p JOIN FETCH p.orders WHERE p.orders.orderId = :orderId")
    Optional<Payments> findByOrderOrderId(@Param("orderId") Long orderId);
    Optional<Payments> findByTransactionCode(String transactionCode);
}
