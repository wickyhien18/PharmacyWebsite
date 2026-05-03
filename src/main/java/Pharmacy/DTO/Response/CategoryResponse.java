package Pharmacy.DTO.Response;

public record CategoryResponse(
        Integer categoryId,
        String categoryName,
        String categorySlug
) {
}
