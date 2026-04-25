package com.example.Pharmacy.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @JsonProperty("refreshToken")
    private String refreshToken;
}
