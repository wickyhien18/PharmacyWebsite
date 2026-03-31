package com.example.Pharmacy.Repositories;

import com.example.Pharmacy.Entities.Medicines;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicines,Long> {

    @Query("Select m from Medicines m where LOWER(m.medicine_name) like LOWER(CONCAT('%',:name,'%'))")
    List<Medicines> findByName(@Param("name") String name);
}
