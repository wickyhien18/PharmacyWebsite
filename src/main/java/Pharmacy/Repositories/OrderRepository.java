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
    @Query("SELECT o FROM Orders o JOIN FETCH o.users where o.users.userId = :userId ORDER BY o.createdAt DESC")
    List<Orders> findByUserUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    Optional<Orders> findByOrderCode(String orderCode);
}
