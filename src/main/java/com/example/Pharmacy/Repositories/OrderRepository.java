package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders,Long> {
    @Query(value = "SELECT * FROM orders WHERE order_id = :id", nativeQuery = true)
    Orders findByIdDetail(@Param("id") Integer id);

    @Query(value = "select * from orders " +
            "join order_item on orders.order_id = order_item.order_id " +
            "join users on orders.user_id = users.user_id", nativeQuery = true)
    List<Orders> getAll();
}
