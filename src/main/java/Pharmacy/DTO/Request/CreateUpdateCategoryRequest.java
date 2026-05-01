package Pharmacy.DTO.Request;

import jakarta.validation.constraints.NotBlank;

public record CreateUpdateCategoryRequest(
        @NotBlank(message = "Tên loại thuốc không được để trống")
        String categoryName,

        @NotBlank(message = "Mô tả loại thuốc không được để trống")
        String description
) {
}
