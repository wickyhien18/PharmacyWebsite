package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Carts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartRepository extends JpaRepository<Carts,Long> {
    @Query(value = "SELECT * FROM carts WHERE cart_id = :id", nativeQuery = true)
    Carts findByIdDetail(@Param("id") Integer id);

    @Query(value = "select * from carts " +
            "join cart_item on carts.cart_id = cart_item.cart_id " +
            "join users on carts.user_id = users.user_id",nativeQuery = true)
    List<Carts> getAll();
}
