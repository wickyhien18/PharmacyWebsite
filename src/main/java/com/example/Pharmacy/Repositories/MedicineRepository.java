package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Medicines;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineRepository extends JpaRepository<Medicines,Long> {
}
