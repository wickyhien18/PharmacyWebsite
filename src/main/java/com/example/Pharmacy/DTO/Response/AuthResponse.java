package com.example.Pharmacy.DTO.Response;

import com.example.Pharmacy.Entities.Roles;

public record AuthResponse(
        String accessToken,        // JWT — client gửi kèm mọi request: "Authorization: Bearer <token>"
        String refreshToken,       // Random bytes — dùng để xin access token mới khi hết hạn
        long   accessTokenExpiresIn, // Số giây còn hiệu lực (900 = 15 phút) — client tự set timer refresh
        UserInfo user              // Thông tin user — hiển thị UI ngay, không cần gọi thêm /me
) {
    public record UserInfo(
            Long   id,             // Dùng làm key cho các request sau: /users/{id}/...
            String email,          // Hiển thị trên navbar / profile
            String fullName,       // Hiển thị tên người dùng
            String phone,          // Hiển thị trong trang profile
            Roles   role,           // CUSTOMER / PHARMACIST / ADMIN — client dùng để ẩn/hiện menu
            String avatarUrl       // URL ảnh đại diện — null nếu chưa upload
    ) {}
}