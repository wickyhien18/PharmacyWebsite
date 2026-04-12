package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Orders,Long> {
    @Query(value = "SELECT * FROM orders WHERE order_id = :id", nativeQuery = true)
    Orders findByIdDetail(@Param("id") Integer id);
}
