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

@RestController
@RequestMapping("/api")
@Tag(name = "Manufacturer API")
@RequiredArgsConstructor
/**
 * Class ManufacturerController.
 * Provides functionality and data modeling for ManufacturerController.
 */
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

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

    @PostMapping("/admin/manufacturers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add Manufacturer [ADMIN]")
    public ResponseEntity<ApiResponse<ManufacturerResponse>> create(
            @Valid @RequestBody CreateManufacturerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(manufacturerService.create(request)));
    }

    @PutMapping("/admin/manufacturers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Manufacture's Information [ADMIN]")
    public ResponseEntity<ApiResponse<ManufacturerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateManufacturerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(manufacturerService.update(id, request)));
    }

    /** DELETE /api/admin/manufacturers/{id} */
    @DeleteMapping("/admin/manufacturers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete Manufacturer's Information [ADMIN]")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        manufacturerService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Delete Successfully"));
    }
}
