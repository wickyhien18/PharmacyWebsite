package Pharmacy.DTO.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

public record RegisterRequest (

    @NotBlank(message = "Username can't be left blank")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    String userName,

    @NotBlank(message = "Password can't be left blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).+$",
            message = "Password must contain Uppercase letters, Lowercase letters, " +
                    "Numbers and Special Characters")
    String password,

    @NotBlank(message = "Fullname can't be left blank")
    String fullName,

    @NotBlank @Email(message = "Email is invalid")
    String email,

    @NotBlank(message = "Phone can't be left blank")
    @Pattern(regexp = "^(?:\\\\+84|0)\\\\d{9}$", message = "Phone is invalid")
    String phone
){}
