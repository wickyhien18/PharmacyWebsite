package Pharmacy.DTO.Request;

import jakarta.validation.constraints.*;

public record CreateUpdateMedicineRequest(
        @NotBlank(message = "Medicines name can't be null")
        String medicineName,

        @NotNull(message = "Category can't be null")
        Integer categoryId,

        @NotNull(message = "Manufacturer can't' be null")
        Integer manufacturerId,

        String description,

        @Positive(message = "Price must be greater than 0")
        @DecimalMin(value = "1000.0", message = "Price must be greater than 1000.0")
        Float price,

        @Positive(message = "Quantity must be greater than 0")
        @Min(value = 10, message = "Quantity must be greater than 10")
        Integer quantity
) {
}
