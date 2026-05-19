package Pharmacy.DTO.Response;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for InventoryResponse.
 * This class is used to map data and handle basic structure.
 */
public record InventoryResponse(
        Long      medicineId,
        String    medicineName,
        Integer   quantity,
        LocalDateTime lastUpdated
) {
}
