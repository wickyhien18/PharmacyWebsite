package Pharmacy.DTO.Response;

import Pharmacy.Entities.Roles;

public record AuthResponse(
        String accessToken,        // JWT — client gửi kèm mọi request: "Authorization: Bearer <token>"
        String refreshToken,       // Random bytes — dùng để xin access token mới khi hết hạn
        String accessTokenExpiresIn, // Số giây còn hiệu lực (900 = 15 phút) — client tự set timer refresh
        UserInfo user              // Thông tin user — hiển thị UI ngay, không cần gọi thêm /me
) {
    public record UserInfo(
            Integer   id,             // Dùng làm key cho các request sau: /users/{id}/...
            String username,       // Hiển thị tên người dùng
            Roles   role           // CUSTOMER / PHARMACIST / ADMIN — client dùng để ẩn/hiện menu
    ) {}
}