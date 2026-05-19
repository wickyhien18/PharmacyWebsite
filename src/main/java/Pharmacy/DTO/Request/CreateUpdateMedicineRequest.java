package Pharmacy.DTO.Request;

import Pharmacy.Entities.Medicines;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for CreateUpdateMedicineRequest.
 * This class is used to map data and handle basic structure.
 */
public record CreateUpdateMedicineRequest(
        @NotBlank(message = "Medicine Name can't be left null") @Size(max = 500) String medicinesName,
        String description,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        String unit,
        Long categoryId,
        Long manufacturerId,
        LocalDate expireDate,
        Medicines.Status status
) {
}
