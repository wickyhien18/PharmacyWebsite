package Pharmacy.DTO.Response;

import java.time.LocalDateTime;

public record InventoryResponse(
        Long      medicineId,
        String    medicineName,
        Integer   quantity,
        LocalDateTime lastUpdated
) {
}
