package Pharmacy.DTO.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotNull Long medicineId,
        @Min(1) Integer quantity
) {
}
