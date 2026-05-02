package Pharmacy.Controllers;

import Pharmacy.DTO.Request.CreateUpdateMedicineRequest;
import Pharmacy.DTO.Response.ApiResponse;
import Pharmacy.DTO.Response.MedicineResponse;
import Pharmacy.Entities.Medicines;
import Pharmacy.Services.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@Tag(name = "Medicines API", description = "Quản lý danh sách thuốc")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @GetMapping
    @Operation(summary = "Lấy danh sách thuốc")
    public ResponseEntity<?> getAll() {
        List<MedicineResponse> medicines = medicineService.getAll();
        return ResponseEntity.ok(ApiResponse.ok(medicines));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin thuốc theo mã")
    public ResponseEntity<Medicines> getById(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("ID không hợp lệ: " + id);
        }

        Medicines medicines = medicineService.getById(id);

        if (medicines == null) {
            throw new RuntimeException("Không tìm thấy thuốc tương ứng với ID: " + id);
        }
        return  ResponseEntity.ok(medicines);
    }

    @GetMapping("/search")
    @Operation(summary = "Lấy danh sách thuốc có chứa tên tìm kiếm")
    public ResponseEntity<List<Medicines>> getByName(@RequestParam (required = false) String name) {
        List<Medicines> medicines =  medicineService.getByName(name);

        if (medicines == null) {
            throw new RuntimeException("Không tìm thấy thông tin thuốc tương ứng với tên thuốc: " + name);
        }

        return ResponseEntity.ok(medicines);
    }

    @PostMapping
    @Operation(summary = "Thêm thông tin thuốc")
    public ResponseEntity<?> createMedicine(@Valid @RequestBody CreateUpdateMedicineRequest medicines) {

        MedicineResponse saved = medicineService.insert(medicines);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Sửa thông tin thuốc")
    public ResponseEntity<Medicines> updateMedicine(@PathVariable Integer id ,@Valid @RequestBody  Medicines medicines) {

        Medicines update = medicineService.update(id,medicines);

        return ResponseEntity.ok(update);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xoá thông tin thuốc")
    public ResponseEntity<Void> deleteMedicine(@PathVariable Integer id) {
        Medicines exist = medicineService.getById(id);
        if (exist == null) {
            throw new RuntimeException("Không tìm thấy thông tin thuốc để xoá");
        }

        medicineService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
