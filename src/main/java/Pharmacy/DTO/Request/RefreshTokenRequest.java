package Pharmacy.DTO.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * Data Transfer Object for RefreshTokenRequest.
 * This class is used to map data and handle basic structure.
 */
public record RefreshTokenRequest (

        @NotBlank
        String refreshToken
){}
