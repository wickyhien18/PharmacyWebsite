package Pharmacy.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserProfile {
    @JsonProperty("userName")
    private String userName;

    @JsonProperty("roleName")
    private String roleName;

}
