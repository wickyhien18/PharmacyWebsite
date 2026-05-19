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

@RestController
@RequestMapping("/api/admin/inventory")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Inventory")
/**
 * Class InventoryController.
 * Provides functionality and data modeling for InventoryController.
 */
public class InventoryController {

    private final InventoryService inventoryService;

    /** GET /api/admin/inventory/{medicineId} */
    @GetMapping("/{medicineId}")
    @Operation(summary = "List of Medicine's Stock")
    public ResponseEntity<ApiResponse<InventoryResponse>> getStock(
            @PathVariable Long medicineId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getStock(medicineId)));
    }

    /** POST /api/admin/inventory/import */
    @PostMapping("/import")
    @Operation(summary = "Import")
    public ResponseEntity<ApiResponse<InventoryResponse>> importStock(
            @Valid @RequestBody ImportStockRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Imported warehouse successfully",
                inventoryService.importStock(request)));
    }

    /** GET /api/admin/inventory/{medicineId}/logs */
    @GetMapping("/{medicineId}/logs")
    @Operation(summary = "Get Inventory Log")
    public ResponseEntity<ApiResponse<List<InventoryLog>>> getLogs(
            @PathVariable Long medicineId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getLogs(medicineId)));
    }
}