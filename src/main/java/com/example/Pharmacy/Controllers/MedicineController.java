package com.example.Pharmacy.Controllers;

import com.example.Pharmacy.Entities.Medicines;
import com.example.Pharmacy.Services.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medicines")
@Tag(name = "Medicines API", description = "Quản lý danh sách thuốc")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @GetMapping("/")
    @Operation(summary = "Lấy danh sách thuốc")
    public List<Medicines> getAll() {
        return medicineService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin thuốc theo mã")
    public ResponseEntity<Medicines> getById(@PathVariable Integer id) {
        System.out.println("=== API /api/medicines/" + id + " được gọi ===");
        Medicines medicines = medicineService.getById(id);
        if (medicines != null) {
            System.out.println("Tìm thấy: ");
            return ResponseEntity.ok(medicines);
        }
        System.out.println("Không tìm thấy ID: " + id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Lấy danh sách thuốc có chứa tên tìm kiếm")
    public List<Medicines> getByName(@RequestParam (required = false) String name) {
        return medicineService.getByName(name);
    }
}
