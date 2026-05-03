package Pharmacy.DTO.Response;

public record ManufacturerResponse(
        Long   manufacturerId,
        String manufacturerName,
        String country
) {
}
