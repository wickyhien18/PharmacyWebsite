package Pharmacy.DTO.Request;

import jakarta.validation.constraints.NotBlank;

public record CreateManufacturerRequest(
        @NotBlank String manufacturerName,
        String country
) {}
