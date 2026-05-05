package Pharmacy.Controllers;

import Pharmacy.DTO.Request.CreateUpdateMedicineRequest;
import Pharmacy.DTO.Response.ApiResponse;
import Pharmacy.DTO.Response.MedicineResponse;
import Pharmacy.Services.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medicines")
@Tag(name = "Medicines API")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    @Operation(summary = "List of Medicines")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String  keyword,
            @RequestParam(required = false) Long    categoryId,
            @RequestParam(required = false) Long    manufacturerId,
            @RequestParam(required = false) String  status,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.ok(
                medicineService.search(keyword, categoryId, manufacturerId, status, pageable)));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get medicine's information from Slug")
    public ResponseEntity<ApiResponse<MedicineResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(medicineService.getBySlug(slug)));
    }


    @PostMapping
    @Operation(summary = "Add medicine's information")
    public ResponseEntity<ApiResponse<MedicineResponse>> create(
            @Valid @RequestBody CreateUpdateMedicineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(medicineService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update medicine's information")
    public ResponseEntity<ApiResponse<MedicineResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateUpdateMedicineRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(medicineService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete medicine's information")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        medicineService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Delete medicine's information successfully"));
    }
}
