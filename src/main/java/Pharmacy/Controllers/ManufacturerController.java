package Pharmacy.Controllers;

import Pharmacy.DTO.Request.CreateManufacturerRequest;
import Pharmacy.DTO.Response.ApiResponse;
import Pharmacy.DTO.Response.ManufacturerResponse;
import Pharmacy.Services.ManufacturerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// Indicates that this class is a REST controller handling HTTP requests.
@RestController
// Maps HTTP requests to the controller or handler method.
@RequestMapping("/api")
@Tag(name = "Manufacturer API")
// Generates a constructor with required arguments (e.g., final fields) via Lombok.
@RequiredArgsConstructor
/**
 * Class ManufacturerController.
 * Provides functionality and data modeling for ManufacturerController.
 */
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    // Maps HTTP GET requests to this handler method.
    @GetMapping("/manufacturers")
    @Operation(summary = "List of Manufacturers")
    /**
     * Retrieves all.
     *
     * @return the ResponseEntity<?> result
     */
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(manufacturerService.getAll()));
    }

    // Maps HTTP POST requests to this handler method.
    @PostMapping("/admin/manufacturers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add Manufacturer [ADMIN]")
    public ResponseEntity<ApiResponse<ManufacturerResponse>> create(
            // Marks a property, method parameter or method return type for validation cascading.
            @Valid @RequestBody CreateManufacturerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(manufacturerService.create(request)));
    }

    // Maps HTTP PUT requests to this handler method.
    @PutMapping("/admin/manufacturers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Manufacture's Information [ADMIN]")
    public ResponseEntity<ApiResponse<ManufacturerResponse>> update(
            @PathVariable Long id,
            // Marks a property, method parameter or method return type for validation cascading.
            @Valid @RequestBody CreateManufacturerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(manufacturerService.update(id, request)));
    }

    /** DELETE /api/admin/manufacturers/{id} */
    // Maps HTTP DELETE requests to this handler method.
    @DeleteMapping("/admin/manufacturers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete Manufacturer's Information [ADMIN]")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        manufacturerService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Delete Successfully"));
    }
}
