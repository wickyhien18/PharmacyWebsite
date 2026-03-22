package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Orders,Long> {
}
