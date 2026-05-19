package Pharmacy.DTO.Response;

/**
 * Data Transfer Object for ManufacturerResponse.
 * This class is used to map data and handle basic structure.
 */
public record ManufacturerResponse(
        Long   manufacturerId,
        String manufacturerName,
        String country
) {
}
