package Pharmacy.Repositories;

import Pharmacy.Entities.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders,Long> {
    List<Orders> findByUserUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Orders> findByOrderCode(String orderCode);
}
