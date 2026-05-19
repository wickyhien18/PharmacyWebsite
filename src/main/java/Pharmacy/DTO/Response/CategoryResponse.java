package Pharmacy.DTO.Response;

/**
 * Data Transfer Object for CategoryResponse.
 * This class is used to map data and handle basic structure.
 */
public record CategoryResponse(
        Long categoryId,
        String categoryName,
        String categorySlug
) {
}
