package Pharmacy.DTO.Response;

/**
 * Data Transfer Object for AuthResponse.
 * This class is used to map data and handle basic structure.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        UserInfo user
) {
/**
 * Data Transfer Object for UserInfo.
 * This class is used to map data and handle basic structure.
 */
    public record UserInfo(
            Long   userId,
            String userName,
            String fullName,
            String email,
            String phone,
            String role         // CUSTOMER / PHARMACIST / ADMIN — client used to hide/show menu
    ) {}
}