# 💊 Pharmacy Web App

Web ứng dụng nhà thuốc trực tuyến — được xây dựng bằng Spring Boot, MySQL và tích hợp thanh toán VNPay. Hệ thống bao gồm đầy đủ luồng mua bán, quản lý giỏ hàng, đặt hàng và tích hợp xác thực bảo mật JWT.

🔗 **Demo:** [Link Demo của bạn]  
📖 **Swagger UI:** [Link Swagger của bạn]

---

## Tech Stack

| Layer | Công nghệ |
|---|---|
| Backend | Java, Spring Boot 3.x |
| Security | Spring Security, JWT (Access Token) + Refresh Token |
| Database | MySQL 8.0, Spring Data JPA |
| Payment | VNPay Sandbox |
| Docs | Springdoc OpenAPI (Swagger UI) |
| Deploy | [Tùy chọn nền tảng: Railway/Render/AWS...] |

---

## Tính năng chính

### Khách hàng
- Đăng ký / Đăng nhập an toàn với JWT (Bao gồm Access Token & Refresh Token).
- Duyệt và tìm kiếm thông tin thuốc (Medicine), nhà sản xuất (Manufacturer), danh mục (Category).
- Giỏ hàng (Cart) — thêm, sửa số lượng, xoá sản phẩm khỏi giỏ hàng.
- Đặt hàng và thanh toán trực tuyến qua cổng VNPay.
- Xem lịch sử đơn hàng, gửi yêu cầu huỷ đơn/hoàn hàng.

### Admin
- Quản lý danh mục (Categories), nhà sản xuất (Manufacturers), sản phẩm (Medicines).
- Quản lý tồn kho (Inventory) — nhập kho, theo dõi lịch sử tồn kho (Inventory Log).
- Xử lý đơn hàng — duyệt đơn, xác nhận giao hàng.
- Quản lý người dùng (Users) và quyền hạn (Roles).
- Duyệt hoặc từ chối các yêu cầu huỷ đơn/hoàn hàng của khách.

### Thanh toán (VNPay)
- Tạo URL thanh toán an toàn với mã hoá HMAC-SHA512.
- Xử lý IPN callback từ VNPay (để cập nhật trạng thái thanh toán tự động server-to-server).
- Return URL để điều hướng người dùng sau khi giao dịch.

---

## Luồng quản lý đơn hàng cơ bản

```text
PENDING      → [User tự huỷ]       → CANCELLED
CONFIRMED    → [User gửi yêu cầu]  → CANCEL_REQUESTED
               [Admin duyệt]        → CANCELLED
               [Admin từ chối]      → CONFIRMED
SHIPPING     → [User gửi yêu cầu]  → RETURN_REQUESTED
               [Admin xác nhận về]  → RETURNED
```

---

## Hướng dẫn cài đặt & Chạy Local

### Yêu cầu
- Java (17 hoặc 21)
- MySQL 8.0+
- Maven

### Bước 1 — Thiết lập Database

Tạo một database trong MySQL (ví dụ: `pharmacy_db`).

### Bước 2 — Cấu hình ứng dụng

Mở file `src/main/resources/application.properties` (hoặc `application.yml`) và cập nhật:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/[Tên_DB_Của_Bạn]?useSSL=false&serverTimezone=UTC
spring.datasource.username=[Tên_Đăng_Nhập_MySQL]
spring.datasource.password=[Mật_Khẩu_MySQL]

# Cấu hình JWT
app.jwt.secret=[Chuỗi_Bí_Mật_JWT_Của_Bạn]

# Cấu hình VNPay
vnpay.tmn-code=[Mã_TMN_Code]
vnpay.hash-secret=[Chuỗi_Hash_Secret]
```

### Bước 3 — Khởi chạy ứng dụng

Sử dụng Maven để chạy:

```bash
mvn spring-boot:run
```

Sau khi chạy thành công, truy cập Swagger UI tại: `http://localhost:8080/swagger-ui.html` (Hoặc port tương ứng của bạn).

---

## Cấu trúc thư mục (Packages)

Toàn bộ logic được tổ chức rõ ràng theo chuẩn mô hình MVC và RESTful API:

```text
src/main/java/Pharmacy/
├── Config/          # Chứa cấu hình Security, JWT, CORS, Swagger, VNPay
├── Controllers/     # Các REST API Endpoint cho Client & Admin
├── Services/        # Chứa Business Logic (Auth, Order, Cart, Medicine...)
├── Repositories/    # Data Access Layer (Kế thừa JpaRepository)
├── Entities/        # Các bảng CSDL (Users, Orders, Cart, Medicine, Inventory...)
├── DTO/             # Data Transfer Objects (Request/Response payload)
└── Exceptions/      # Global Exception Handler & Custom Exceptions
```

---

## Tác giả

**[Tên của bạn]**  
Vai trò / Công việc: [Mô tả ngắn gọn / Sinh viên...]  
📧 Email: [Địa chỉ Email của bạn]  
🔗 GitHub: [Link Github của bạn]  
🔗 LinkedIn: [Link LinkedIn của bạn]  
