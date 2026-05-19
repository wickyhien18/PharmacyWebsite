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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// Indicates that this class is a REST controller handling HTTP requests.
@RestController
// Maps HTTP requests to the controller or handler method.
@RequestMapping("/api")
@Tag(name = "Medicines API")
// Generates a constructor with required arguments (e.g., final fields) via Lombok.
@RequiredArgsConstructor
/**
 * Class MedicineController.
 * Provides functionality and data modeling for MedicineController.
 */
public class MedicineController {

    private final MedicineService medicineService;

    // Maps HTTP GET requests to this handler method.
    @GetMapping("/medicines/")
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

    // Maps HTTP GET requests to this handler method.
    @GetMapping("/medicines/{slug}")
    @Operation(summary = "Get medicine's information from Slug")
    /**
     * Retrieves by slug.
     *
     * @param slug the slug
     * @return the ResponseEntity<ApiResponse<MedicineResponse>> result
     */
    public ResponseEntity<ApiResponse<MedicineResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(medicineService.getBySlug(slug)));
    }


    // Maps HTTP POST requests to this handler method.
    @PostMapping("/admin/medicines")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add medicine's information")
    public ResponseEntity<ApiResponse<MedicineResponse>> create(
            // Marks a property, method parameter or method return type for validation cascading.
            @Valid @RequestBody CreateUpdateMedicineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(medicineService.create(request)));
    }

    // Maps HTTP PUT requests to this handler method.
    @PutMapping("/admin/medicines/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update medicine's information")
    public ResponseEntity<ApiResponse<MedicineResponse>> update(
            @PathVariable Long id,
            // Marks a property, method parameter or method return type for validation cascading.
            @Valid @RequestBody CreateUpdateMedicineRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(medicineService.update(id, request)));
    }

    // Maps HTTP DELETE requests to this handler method.
    @DeleteMapping("/admin/medicines/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete medicine's information")
    /**
     * Deletes .
     *
     * @param id the id
     * @return the ResponseEntity<?> result
     */
    public ResponseEntity<?> delete(@PathVariable Long id) {
        medicineService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Delete medicine's information successfully"));
    }
}
