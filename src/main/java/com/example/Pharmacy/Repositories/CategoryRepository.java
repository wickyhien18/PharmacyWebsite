package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Categories, Long> {
}
