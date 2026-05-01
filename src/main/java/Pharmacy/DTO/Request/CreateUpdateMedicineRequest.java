package Pharmacy.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUpdateMedicineRequest(
        @NotBlank(message = "Tên thuốc không được bỏ trống")
        String medicineName,

        @NotNull(message = "Loại thuốc không được để trống")
        Integer categoryId,

        @NotNull(message = "Nhà xản xuất không được để trống")
        Integer manufacturerId,

        String description,
        Float price,
        Integer quantity
) {
}
