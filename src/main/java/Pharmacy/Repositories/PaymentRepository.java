package Pharmacy.Repositories;

import Pharmacy.Entities.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payments,Long> {
    Optional<Payments> findByOrderOrderId(Long orderId);
    Optional<Payments> findByTransactionCode(String transactionCode);
}
