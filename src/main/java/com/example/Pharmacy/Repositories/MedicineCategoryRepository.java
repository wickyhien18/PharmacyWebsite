package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.MedicineCategories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineCategoryRepository extends JpaRepository<MedicineCategories,Long> {
}
