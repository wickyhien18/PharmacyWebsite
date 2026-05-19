package Pharmacy.DTO.Request;

import jakarta.validation.constraints.Min;

/**
 * Data Transfer Object for UpdateCartItemRequest.
 * This class is used to map data and handle basic structure.
 */
public record UpdateCartItemRequest(
        @Min(0) Integer quantity
) {
}
