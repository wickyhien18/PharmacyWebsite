package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payments,Long> {
    @Query(value = "SELECT * FROM payments WHERE payment_id = :id", nativeQuery = true)
    Payments findByIdDetail(@Param("id") Integer id);

    @Query(value = " select * from payments join orders on payments.order_id = orders.order_id", nativeQuery = true)
    List<Payments> getAll();
}
