package Pharmacy.DTO.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ImportStockRequest(
        @NotNull Long medicineId,
        @Min(1) Integer quantity,
        String note
) {
}
