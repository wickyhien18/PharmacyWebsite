package Pharmacy.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateCategoryRequest(

        @NotBlank
        String categoryName,

        @NotBlank @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug include lowercase letters, numbers and dash")
        String slug
) {
}
