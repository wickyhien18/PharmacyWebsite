# 💊 Pharmacy Web App

Web ứng dụng nhà thuốc trực tuyến tương tự **NhathuocLongChau.com.vn** — được xây dựng bằng Spring Boot, MySQL và tích hợp thanh toán VNPay.

🔗 **Demo:** https://pharmacy-app.up.railway.app  
📖 **Swagger UI:** https://pharmacy-app.up.railway.app/swagger-ui.html

---

## Tech Stack

| Layer | Công nghệ |
|---|---|
| Backend | Java 21, Spring Boot 3.x |
| Security | Spring Security, JWT (Access Token) + Refresh Token |
| Database | MySQL 8.0, Spring Data JPA |
| Payment | VNPay Sandbox |
| Docs | Springdoc OpenAPI (Swagger UI) |
| Deploy | Railway (free tier) |
| Test | JUnit 5, Mockito |

---

## Tính năng chính

### Khách hàng
- Đăng ký / Đăng nhập với JWT — access token 1 giờ, refresh token 7 ngày
- Duyệt và tìm kiếm thuốc theo tên, danh mục, nhà sản xuất
- Giỏ hàng — thêm, sửa số lượng, xoá sản phẩm
- Đặt hàng và thanh toán (COD hoặc VNPay)
- Xem lịch sử đơn hàng, huỷ đơn khi đang chờ xác nhận
- Gửi yêu cầu huỷ / hoàn hàng sau khi đơn đã xác nhận

### Admin
- Quản lý danh mục, nhà sản xuất, sản phẩm (CRUD)
- Quản lý tồn kho — nhập kho, xem lịch sử xuất/nhập
- Xử lý đơn hàng — duyệt, xác nhận giao hàng
- Duyệt hoặc từ chối yêu cầu huỷ đơn của khách
- Xác nhận hoàn hàng khi hàng về kho

### Thanh toán (VNPay)
- Tạo link thanh toán, link có thời hạn 15 phút
- Xử lý IPN callback từ VNPay (server-to-server)
- Chống duplicate IPN — kiểm tra trạng thái trước khi xử lý
- Lưu raw callback để debug tranh chấp

---

## Luồng huỷ đơn hàng

```
PENDING      → [User tự huỷ]       → CANCELLED          (hoàn kho ngay)
CONFIRMED    → [User gửi yêu cầu]  → CANCEL_REQUESTED
               [Admin duyệt]        → CANCELLED          (hoàn kho + refund)
               [Admin từ chối]      → CONFIRMED          (tiếp tục bình thường)
SHIPPING     → [User gửi yêu cầu]  → RETURN_REQUESTED
               [Admin xác nhận về]  → RETURNED           (hoàn kho + refund)
```

---

## Chạy local

### Yêu cầu
- Java 21+
- MySQL 8.0
- Maven 3.9+

### Bước 1 — Tạo database

```sql
-- Chạy file này trên MySQL Workbench
source src/main/resources/schema_final.sql
```

### Bước 2 — Cấu hình

```properties
# src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/pharmacy_db?...
spring.datasource.username=root
spring.datasource.password=your_password

app.jwt.secret=your_base64_secret
vnpay.tmn-code=your_tmn_code
vnpay.hash-secret=your_hash_secret
```

### Bước 3 — Chạy

```bash
mvn spring-boot:run
```

Truy cập Swagger UI: http://localhost:8080/swagger-ui.html

---

## Chạy bằng Docker

```bash
# 1. Copy file env
cp .env.example .env
# Điền giá trị thật vào .env

# 2. Build và chạy
docker-compose up -d

# 3. Kiểm tra logs
docker-compose logs -f app
```

---

## Cấu trúc project

```
src/main/java/com/pharmacy/
├── config/          # SecurityConfig
├── controller/      # REST Controllers
├── service/         # Business Logic
├── repository/      # Spring Data JPA
├── entity/          # JPA Entities (14 bảng)
├── dto/             # Request / Response DTOs
├── exception/       # AppException + GlobalExceptionHandler
└── security/        # JwtUtil, JwtFilter, VNPayUtil
```

---

## API Endpoints chính

```
POST /api/auth/register          Đăng ký
POST /api/auth/login             Đăng nhập
POST /api/auth/refresh           Làm mới token
POST /api/auth/logout            Đăng xuất

GET  /api/medicines              Danh sách thuốc + filter + phân trang
GET  /api/medicines/{slug}       Chi tiết thuốc
GET  /api/categories             Danh sách danh mục

GET  /api/cart                   Xem giỏ hàng
POST /api/cart/items             Thêm vào giỏ
PATCH /api/cart/items/{id}       Cập nhật số lượng

POST /api/orders                 Đặt hàng
GET  /api/orders                 Lịch sử đơn hàng
POST /api/orders/{id}/cancel     Huỷ đơn (khi PENDING)
POST /api/orders/{id}/request-cancel   Yêu cầu huỷ (khi CONFIRMED)
POST /api/orders/{id}/request-return   Yêu cầu hoàn (khi SHIPPING)

POST /api/payment/vnpay/create/{orderId}  Tạo link thanh toán
GET  /api/payment/vnpay-return            Return URL sau khi trả tiền
GET  /api/payment/vnpay-ipn              IPN callback từ VNPay
```

---

## Test

```bash
# Chạy tất cả test
mvn test

# Chạy 1 class test cụ thể
mvn test -Dtest=OrderServiceTest

# Xem coverage report
mvn test jacoco:report
# Mở target/site/jacoco/index.html
```

**Bộ test hiện tại:** 47 unit test cho `AuthService`, `OrderService`, `PaymentService`  
**Coverage:** ~65% Service layer

---

## Những gì chưa implement (trả lời thành thật khi phỏng vấn)

| Tính năng | Lý do chưa làm |
|---|---|
| Redis cache | Chưa cần ở scale hiện tại, sẽ thêm nếu có bottleneck |
| Gọi VNPay Refund API thật | Hiện đánh dấu REFUNDED thủ công, cần account production |
| Email notification | Thiếu SMTP config, có thể dùng Mailjet free |
| Upload ảnh thuốc | Có thể tích hợp Cloudinary, hiện chỉ lưu URL |
| Full-text search | Đang dùng LIKE, có thể nâng lên FULLTEXT index |
| Tích hợp GHN/GHTK | Shipment hiện chỉ track trong DB |

---

## Tác giả

**[Tên của bạn]**  
Sinh viên năm 3 — Đại học [Tên trường]  
📧 email@example.com  
🔗 github.com/username
