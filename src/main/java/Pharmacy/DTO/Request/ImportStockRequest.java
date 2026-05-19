package Pharmacy.DTO.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for ImportStockRequest.
 * This class is used to map data and handle basic structure.
 */
public record ImportStockRequest(
        @NotNull Long medicineId,
        @Min(1) Integer quantity,
        String note
) {
}
