package Pharmacy.DTO.Request;

import jakarta.validation.constraints.Min;

public record UpdateCartItemRequest(
        @Min(0) Integer quantity
) {
}
