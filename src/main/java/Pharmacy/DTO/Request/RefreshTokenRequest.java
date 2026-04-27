package Pharmacy.DTO.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @JsonProperty("refreshToken")
    private String refreshToken;
}
