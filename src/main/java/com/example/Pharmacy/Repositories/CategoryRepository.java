package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Categories, Long> {

    @Query(value = "SELECT * FROM categories WHERE category_id = :id", nativeQuery = true)
    Categories findByIdDetail(@Param("id") Integer id);
}
