package Pharmacy.DTO.Response;

public record CategoryResponse(
        Long categoryId,
        String categoryName,
        String categorySlug
) {
}
