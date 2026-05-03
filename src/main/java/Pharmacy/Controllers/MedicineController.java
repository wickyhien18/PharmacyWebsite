//package Pharmacy.Controllers;
//
//import Pharmacy.DTO.Request.CreateUpdateMedicineRequest;
//import Pharmacy.DTO.Response.ApiResponse;
//import Pharmacy.DTO.Response.MedicineResponse;
//import Pharmacy.Entities.Medicines;
//import Pharmacy.Services.MedicineService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/medicines")
//@Tag(name = "Medicines API")
//public class MedicineController {
//
//    @Autowired
//    private MedicineService medicineService;
//
//    @GetMapping
//    @Operation(summary = "List of Medicines")
//    public ResponseEntity<?> getAll() {
//        List<MedicineResponse> medicines = medicineService.getAll();
//        return ResponseEntity.ok(ApiResponse.ok(medicines));
//    }
//
//    @GetMapping("/{id}")
//    @Operation(summary = "Get medicine's information from Id")
//    public ResponseEntity<?> getById(@PathVariable Long id) {
//
//        MedicineResponse medicines = medicineService.getById(id);
//
//        return  ResponseEntity.ok(ApiResponse.ok(medicines));
//    }
//
//
//    @PostMapping
//    @Operation(summary = "Thêm thông tin thuốc")
//    public ResponseEntity<?> createMedicine(@Valid @RequestBody CreateUpdateMedicineRequest medicines) {
//
//        MedicineResponse saved = medicineService.insert(medicines);
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(ApiResponse.ok(saved));
//    }
//
//    @PutMapping("/{id}")
//    @Operation(summary = "Sửa thông tin thuốc")
//    public ResponseEntity<Medicines> updateMedicine(@PathVariable Integer id ,@Valid @RequestBody  Medicines medicines) {
//
//        Medicines update = medicineService.update(id,medicines);
//
//        return ResponseEntity.ok(update);
//    }
//
//    @DeleteMapping("/{id}")
//    @Operation(summary = "Xoá thông tin thuốc")
//    public ResponseEntity<Void> deleteMedicine(@PathVariable Integer id) {
//        Medicines exist = medicineService.getById(id);
//        if (exist == null) {
//            throw new RuntimeException("Không tìm thấy thông tin thuốc để xoá");
//        }
//
//        medicineService.delete(id);
//
//        return ResponseEntity.noContent().build();
//    }
//}
