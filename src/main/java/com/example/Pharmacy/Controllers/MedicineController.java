package com.example.Pharmacy.Controllers;

import com.example.Pharmacy.Entities.Medicines;
import com.example.Pharmacy.Exceptions.BadRequestException;
import com.example.Pharmacy.Exceptions.ResourceNotFoundException;
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
@RequestMapping("/api/medicines")
@Tag(name = "Medicines API", description = "Quản lý danh sách thuốc")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @GetMapping
    @Operation(summary = "Lấy danh sách thuốc")
    public ResponseEntity<List<Medicines>> getAll() {
        List<Medicines> medicines = medicineService.getAll();
        return ResponseEntity.ok(medicines);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin thuốc theo mã")
    public ResponseEntity<Medicines> getById(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("ID không hợp lệ: " + id);
        }

        Medicines medicines = medicineService.getById(id);

        if (medicines == null) {
            throw new ResourceNotFoundException("Không tìm thấy thuốc tương ứng với ID: " + id);
        }
        return  ResponseEntity.ok(medicines);
    }

    @GetMapping("/search")
    @Operation(summary = "Lấy danh sách thuốc có chứa tên tìm kiếm")
    public ResponseEntity<List<Medicines>> getByName(@RequestParam (required = false) String name) {
        List<Medicines> medicines =  medicineService.getByName(name);

        if (medicines == null) {
            throw new ResourceNotFoundException("Không tìm thấy thông tin thuốc tương ứng với tên thuốc: " + name);
        }

        return ResponseEntity.ok(medicines);
    }
//
//    @PostMapping
//    @Operation(summary = "Thêm thông tin thuốc")
//    public ResponseEntity<Medicines> createMedicine(@RequestBody Medicines medicines) {
//        if (medicines.getMedicine_name().trim().isEmpty() || medicines.ge) {
//            throw new IllegalAccessException("Tên thuốc không được để ")
//        }
//    }
}
