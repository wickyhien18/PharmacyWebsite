package Pharmacy.Controllers;

import Pharmacy.DTO.Request.ImportStockRequest;
import Pharmacy.DTO.Response.ApiResponse;
import Pharmacy.DTO.Response.InventoryResponse;
import Pharmacy.Entities.InventoryLog;
import Pharmacy.Services.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Indicates that this class is a REST controller handling HTTP requests.
@RestController
// Maps HTTP requests to the controller or handler method.
@RequestMapping("/api/admin/inventory")
@PreAuthorize("hasRole('ADMIN')")
// Generates a constructor with required arguments (e.g., final fields) via Lombok.
@RequiredArgsConstructor
@Tag(name = "Inventory")
/**
 * Class InventoryController.
 * Provides functionality and data modeling for InventoryController.
 */
public class InventoryController {

    private final InventoryService inventoryService;

    /** GET /api/admin/inventory/{medicineId} */
    // Maps HTTP GET requests to this handler method.
    @GetMapping("/{medicineId}")
    @Operation(summary = "List of Medicine's Stock")
    public ResponseEntity<ApiResponse<InventoryResponse>> getStock(
            @PathVariable Long medicineId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getStock(medicineId)));
    }

    /** POST /api/admin/inventory/import */
    // Maps HTTP POST requests to this handler method.
    @PostMapping("/import")
    @Operation(summary = "Import")
    public ResponseEntity<ApiResponse<InventoryResponse>> importStock(
            // Marks a property, method parameter or method return type for validation cascading.
            @Valid @RequestBody ImportStockRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Imported warehouse successfully",
                inventoryService.importStock(request)));
    }

    /** GET /api/admin/inventory/{medicineId}/logs */
    // Maps HTTP GET requests to this handler method.
    @GetMapping("/{medicineId}/logs")
    @Operation(summary = "Get Inventory Log")
    public ResponseEntity<ApiResponse<List<InventoryLog>>> getLogs(
            @PathVariable Long medicineId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getLogs(medicineId)));
    }
}