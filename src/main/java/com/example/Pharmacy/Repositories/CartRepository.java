package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Carts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Carts,Long> {
    @Query(value = "SELECT * FROM carts WHERE cart_id = :id", nativeQuery = true)
    Carts findByIdDetail(@Param("id") Integer id);
}
