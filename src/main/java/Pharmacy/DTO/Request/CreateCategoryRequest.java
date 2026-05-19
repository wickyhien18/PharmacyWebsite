package Pharmacy.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for CreateCategoryRequest.
 * This class is used to map data and handle basic structure.
 */
public record CreateCategoryRequest(

        @NotBlank
        String categoryName,

        @NotBlank @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug include lowercase letters, numbers and dash")
        String slug
) {
}
