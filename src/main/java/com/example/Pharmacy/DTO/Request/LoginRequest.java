package com.example.Pharmacy.DTO.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @JsonProperty("userName")
    @NotBlank
    private String userName;

    @JsonProperty("password")
    @NotBlank
    private String password;
}
