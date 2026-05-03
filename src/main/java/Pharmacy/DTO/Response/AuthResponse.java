package Pharmacy.DTO.Response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        UserInfo user
) {
    public record UserInfo(
            Long   userId,
            String userName,
            String fullName,
            String email,
            String phone,
            String role         // CUSTOMER / PHARMACIST / ADMIN — client dùng để ẩn/hiện menu
    ) {}
}