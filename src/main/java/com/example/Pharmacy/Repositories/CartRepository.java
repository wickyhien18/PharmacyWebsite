package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Carts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Carts,Long> {
}
