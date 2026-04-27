package Pharmacy.Repositories;

import Pharmacy.Entities.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders,Long> {
    @Query(value = "SELECT * FROM orders WHERE order_id = :id", nativeQuery = true)
    Orders findByIdDetail(@Param("id") Integer id);

    @Query("SELECT o,oi,u from Orders o join o.orderItems oi join o.users u")
    List<Orders> getAll();
}
