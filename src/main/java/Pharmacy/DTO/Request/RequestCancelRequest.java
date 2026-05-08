package Pharmacy.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestCancelRequest(
        @NotBlank(message = "Cancel's Reason can't be left blank")
        @Size(max = 500)
        String reason
) {
}
