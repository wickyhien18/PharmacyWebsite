package com.example.Pharmacy.Controllers;

import com.example.Pharmacy.Entities.Medicines;
import com.example.Pharmacy.Repositories.MedicineRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicines")
@Tag(name = "Medicines API", description = "Quản lý danh sách thuốc")
public class MedicineController {

    @Autowired
    public MedicineRepository medicineRepository;

    @GetMapping("/")
    @Operation(summary = "Lấy danh sách thuốc")
    public List<Medicines> getAll() {
        return medicineRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin thuốc theo mã")
    public Medicines getById(@PathVariable Long id) {
        return medicineRepository.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Lấy danh sách thuốc theo tên")
    public List<Medicines> getByName(@RequestParam(required = false) String name) {
        if (name == null || name.trim().isEmpty()) {
            return medicineRepository.findAll();
        }
        return medicineRepository.findByName(name);
    }
}
