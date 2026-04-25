package com.example.Pharmacy.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RefreshRequest {

    @JsonProperty("refreshToken")
    private String refreshToken;
}
