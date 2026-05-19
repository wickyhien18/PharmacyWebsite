package Pharmacy.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for RejectCancelRequest.
 * This class is used to map data and handle basic structure.
 */
public record RejectCancelRequest(
        @NotBlank(message = "Reject's Reason can't be left blank")
        @Size(max = 500)
        String reason
) {
}
