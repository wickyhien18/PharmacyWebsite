package Pharmacy.DTO.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


public record LoginRequest (

    @JsonProperty("UserName or Email")
    @NotBlank
    String userName,

    @NotBlank
    String password
){}
