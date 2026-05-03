package Pharmacy.DTO.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


public record RefreshTokenRequest (

        @NotBlank
        String refreshToken
){}
