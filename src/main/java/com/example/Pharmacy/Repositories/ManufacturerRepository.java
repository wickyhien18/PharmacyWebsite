package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Manufacturers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufacturerRepository extends JpaRepository<Manufacturers, Long> {
}
