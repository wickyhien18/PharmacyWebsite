package Pharmacy.DTO.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

// Produces complex builder APIs for this class via Lombok.
@Builder
// Generates boilerplate code like getters, setters, toString, equals, and hashCode via Lombok.
@Data
/**
 * Data Transfer Object for LoginResponse.
 * This class is used to map data and handle basic structure.
 */
public class LoginResponse {

    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("refreshToken")
    private String refreshToken;
}
