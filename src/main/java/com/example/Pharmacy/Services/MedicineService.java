package com.example.Pharmacy.Services;

import com.example.Pharmacy.Entities.Medicines;
import com.example.Pharmacy.Repositories.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    public List<Medicines> getAll() {
        return medicineRepository.findAll();
    }

    public Medicines getById(@PathVariable Integer id) {
        return medicineRepository.findByIdDetail(id);
    }

    public List<Medicines> getByName(@RequestParam(required = false) String name) {
        if (name == null || name.trim().isEmpty()) {
            return medicineRepository.findAll();
        }
        return medicineRepository.findByName(name);
    }
}
