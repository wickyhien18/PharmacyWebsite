package Pharmacy.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for MedicineResponse.
 * This class is used to map data and handle basic structure.
 */
public record MedicineResponse(
        Long                medicineId,
        String              medicineName,
        String              medicineSlug,
        String              description,
        BigDecimal          price,
        String              unit,
        String              status,
        LocalDate           expireDate,
        Integer             stockQuantity,   // Lấy từ bảng inventory
        CategoryResponse    category,
        ManufacturerResponse manufacturer,
        LocalDateTime       createdAt
) {
}
