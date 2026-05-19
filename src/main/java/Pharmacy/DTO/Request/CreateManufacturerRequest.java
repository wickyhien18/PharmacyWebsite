package Pharmacy.DTO.Request;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for CreateManufacturerRequest.
 * This class is used to map data and handle basic structure.
 */
public record CreateManufacturerRequest(
        @NotBlank String manufacturerName,
        String country
) {}
