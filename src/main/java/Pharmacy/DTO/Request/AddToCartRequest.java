package Pharmacy.DTO.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for AddToCartRequest.
 * This class is used to map data and handle basic structure.
 */
public record AddToCartRequest(
        @NotNull Long medicineId,
        @Min(1) Integer quantity
) {
}
