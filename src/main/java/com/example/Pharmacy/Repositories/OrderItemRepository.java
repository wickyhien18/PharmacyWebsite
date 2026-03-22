package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItems,Long> {
}
