package Pharmacy.DTO.Request;

import jakarta.validation.constraints.NotBlank;


/**
 * Data Transfer Object for LoginRequest.
 * This class is used to map data and handle basic structure.
 */
public record LoginRequest (
    @NotBlank
    String email,

    @NotBlank
    String password
){}
