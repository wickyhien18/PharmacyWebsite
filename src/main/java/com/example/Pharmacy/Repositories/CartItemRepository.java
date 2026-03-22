package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItems,Long> {
}
