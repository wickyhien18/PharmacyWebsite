CREATE DATABASE IF NOT EXISTS PharmacyDB
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE PharmacyDB;

CREATE TABLE roles (
                       role_id   BIGINT       NOT NULL AUTO_INCREMENT,
                       role_name VARCHAR(100) NOT NULL UNIQUE,
                       PRIMARY KEY (role_id)
);

-- ----------------------------------------------------------------
-- 2. USERS
-- ----------------------------------------------------------------
CREATE TABLE users (
                       user_id       BIGINT       NOT NULL AUTO_INCREMENT,
                       user_name     VARCHAR(100) NOT NULL UNIQUE,
                       password      VARCHAR(255) NOT NULL,
                       full_name     VARCHAR(255) NOT NULL,
                       email         VARCHAR(255) NOT NULL UNIQUE,
                       phone         VARCHAR(20)  NOT NULL UNIQUE,
                       role_id       BIGINT,
                       is_active     TINYINT(1)   DEFAULT 1,
                       created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                       updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       last_activity TIMESTAMP    NULL,
                       deleted_at    TIMESTAMP    NULL,
                       PRIMARY KEY (user_id),
                       CONSTRAINT fk_user_role
                           FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE SET NULL
);

-- ----------------------------------------------------------------
-- 3. REFRESH_TOKENS — lưu random string, không phải JWT
-- ----------------------------------------------------------------
CREATE TABLE refresh_tokens (
                                id         BIGINT       NOT NULL AUTO_INCREMENT,
                                token      VARCHAR(500) NOT NULL UNIQUE,
                                user_id    BIGINT       NOT NULL,
                                expire_at  TIMESTAMP    NULL,
                                created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (id),
                                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------
-- 4. CATEGORIES
-- ----------------------------------------------------------------
CREATE TABLE categories (
                            category_id BIGINT       NOT NULL AUTO_INCREMENT,
                            name        VARCHAR(255) NOT NULL,
                            slug        VARCHAR(255) NOT NULL UNIQUE,
                            PRIMARY KEY (category_id)
);

-- ----------------------------------------------------------------
-- 5. MANUFACTURERS
-- ----------------------------------------------------------------
CREATE TABLE manufacturers (
                               manufacturer_id BIGINT       NOT NULL AUTO_INCREMENT,
                               name            VARCHAR(255) NOT NULL,
                               country         VARCHAR(255) NULL,
                               PRIMARY KEY (manufacturer_id)
);

-- ----------------------------------------------------------------
-- 6. MEDICINES
-- ----------------------------------------------------------------
CREATE TABLE medicines (
                           medicine_id     BIGINT        NOT NULL AUTO_INCREMENT,
                           name            VARCHAR(500)  NOT NULL,
                           slug            VARCHAR(520)  NOT NULL UNIQUE,
                           image           VARCHAR(500)  NULL,
                           description     TEXT          NULL,
                           price           DECIMAL(15,2) NOT NULL,
                           unit            VARCHAR(50)   DEFAULT 'Hộp',
                           category_id     BIGINT        NULL,
                           manufacturer_id BIGINT        NULL,
                           status          ENUM('ACTIVE','INACTIVE','OUT_OF_STOCK') DEFAULT 'ACTIVE',
                           expire_date     DATE          NULL,
                           created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
                           updated_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           deleted_at      TIMESTAMP     NULL,
                           PRIMARY KEY (medicine_id),
                           INDEX idx_category     (category_id),
                           INDEX idx_manufacturer (manufacturer_id),
                           FOREIGN KEY (category_id)     REFERENCES categories(category_id)       ON DELETE SET NULL,
                           FOREIGN KEY (manufacturer_id) REFERENCES manufacturers(manufacturer_id) ON DELETE SET NULL
);

-- ----------------------------------------------------------------
-- 7. INVENTORY — tồn kho hiện tại (1-1 với medicines)
-- ----------------------------------------------------------------
CREATE TABLE inventory (
                           inventory_id BIGINT    NOT NULL AUTO_INCREMENT,
                           medicine_id  BIGINT    NOT NULL,
                           quantity     INT       NOT NULL DEFAULT 0,
                           last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (inventory_id),
                           UNIQUE KEY uk_inventory_medicine (medicine_id),
                           FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------
-- 8. INVENTORY_LOGS — lịch sử nhập/xuất/điều chỉnh kho
-- ----------------------------------------------------------------
CREATE TABLE inventory_logs (
                                log_id            BIGINT      NOT NULL AUTO_INCREMENT,
                                medicine_id       BIGINT      NOT NULL,
                                change_type       ENUM('IMPORT','EXPORT','ADJUST') NOT NULL,
                                quantity          INT         NOT NULL,
                                previous_quantity INT         NOT NULL,
                                new_quantity      INT         NOT NULL,
                                reference_id      BIGINT      NULL,         -- order_id nếu là bán hàng
                                note              VARCHAR(500) NULL,
                                created_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (log_id),
                                INDEX idx_medicine (medicine_id),
                                INDEX idx_type     (change_type),
                                FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------
-- 9. CARTS — header giỏ hàng (1 user 1 cart)
-- ----------------------------------------------------------------
CREATE TABLE carts (
                       cart_id    BIGINT    NOT NULL AUTO_INCREMENT,
                       user_id    BIGINT    NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       PRIMARY KEY (cart_id),
                       FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------
-- 10. CART_ITEMS
-- ----------------------------------------------------------------
CREATE TABLE cart_items (
                            cart_item_id BIGINT NOT NULL AUTO_INCREMENT,
                            cart_id      BIGINT NOT NULL,
                            medicine_id  BIGINT NOT NULL,
                            quantity     INT    NOT NULL DEFAULT 1,
                            PRIMARY KEY (cart_item_id),
                            UNIQUE KEY uk_cart_medicine (cart_id, medicine_id),
                            FOREIGN KEY (cart_id)     REFERENCES carts(cart_id)       ON DELETE CASCADE,
                            FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------
-- 11. ORDERS
-- Bao gồm cả update từ schema_v2 (REFUNDED) và schema_v3 (RETURN_REQUESTED, RETURNED)
-- ----------------------------------------------------------------
CREATE TABLE orders (
                        order_id         BIGINT        NOT NULL AUTO_INCREMENT,
                        user_id          BIGINT        NULL,
                        order_code       VARCHAR(100)  NOT NULL UNIQUE,
                        total_price      DECIMAL(15,2) NOT NULL,

                        order_status     ENUM(
                            'PENDING',           -- Vừa đặt, chờ xác nhận
                            'CONFIRMED',         -- Đã xác nhận, chuẩn bị hàng
                            'SHIPPING',          -- Đang giao
                            'DELIVERED',         -- Giao thành công
                            'CANCEL_REQUESTED',  -- User yêu cầu huỷ khi đang CONFIRMED
                            'CANCELLED',         -- Đã huỷ
                            'RETURN_REQUESTED',  -- User yêu cầu hoàn hàng khi đang SHIPPING
                            'RETURNED'           -- Hàng đã về kho → hoàn tiền + hoàn kho
                            ) DEFAULT 'PENDING',

                        payment_status   ENUM(
                            'PENDING',           -- Chờ thanh toán
                            'PAID',              -- Đã thanh toán
                            'FAILED',            -- Thanh toán thất bại
                            'REFUNDED'           -- Đã hoàn tiền
                            ) DEFAULT 'PENDING',

                        shipping_address VARCHAR(500) NULL,
                        note             TEXT         NULL,

    -- Thông tin huỷ/hoàn hàng
                        cancelled_by     VARCHAR(20)  NULL,      -- 'USER' hoặc 'ADMIN'
                        cancelled_reason VARCHAR(500) NULL,      -- Lý do huỷ / hoàn
                        cancelled_at     TIMESTAMP    NULL,

                        created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                        updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        PRIMARY KEY (order_id),
                        INDEX idx_user   (user_id),
                        INDEX idx_status (order_status),
                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ----------------------------------------------------------------
-- 12. ORDER_ITEMS
-- ----------------------------------------------------------------
CREATE TABLE order_items (
                             order_item_id BIGINT        NOT NULL AUTO_INCREMENT,
                             order_id      BIGINT        NOT NULL,
                             medicine_id   BIGINT        NULL,         -- SET NULL khi medicine bị xoá
                             quantity      INT           NOT NULL,
                             unit_price    DECIMAL(15,2) NOT NULL,     -- Snapshot giá lúc đặt
                             total_price   DECIMAL(15,2) NOT NULL,
                             PRIMARY KEY (order_item_id),
                             INDEX idx_order (order_id),
                             FOREIGN KEY (order_id)    REFERENCES orders(order_id)       ON DELETE CASCADE,
                             FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) ON DELETE SET NULL
);

-- ----------------------------------------------------------------
-- 13. PAYMENTS
-- Bao gồm update từ schema_v2: expired_at, raw_callback, attempt_count, REFUNDED
-- ----------------------------------------------------------------
CREATE TABLE payments (
                          payment_id       BIGINT        NOT NULL AUTO_INCREMENT,
                          order_id         BIGINT        NOT NULL,

                          payment_method   ENUM('COD','VNPAY','MOMO') NOT NULL,
                          amount           DECIMAL(15,2) NOT NULL,
                          transaction_code VARCHAR(255)  NULL,       -- Mã từ VNPay/Momo

                          status           ENUM(
                              'PENDING',             -- Chờ thanh toán
                              'SUCCESS',             -- Thanh toán thành công
                              'FAILED',              -- Thất bại / bị huỷ
                              'REFUNDED'             -- Đã hoàn tiền
                              ) DEFAULT 'PENDING',

                          paid_at          TIMESTAMP     NULL,       -- Thời điểm thanh toán thành công
                          expired_at       TIMESTAMP     NULL,       -- Link VNPay hết hạn sau 15 phút
                          raw_callback     JSON          NULL,       -- Raw payload VNPay gửi về (debug)
                          attempt_count    INT           NOT NULL DEFAULT 0,  -- Số lần user thử thanh toán

                          created_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
                          updated_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          PRIMARY KEY (payment_id),
                          INDEX idx_order  (order_id),
                          INDEX idx_status (status),
                          FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------
-- 14. SHIPMENTS
-- ----------------------------------------------------------------
CREATE TABLE shipments (
                           shipment_id  BIGINT       NOT NULL AUTO_INCREMENT,
                           order_id     BIGINT       NOT NULL,
                           tracking_code VARCHAR(100) NULL,
                           carrier       VARCHAR(100) NULL,          -- GHN, GHTK, VNPost...
                           status        ENUM('PENDING','SHIPPING','DELIVERED','FAILED') NULL,
                           shipped_at    TIMESTAMP    NULL,
                           delivered_at  TIMESTAMP    NULL,
                           PRIMARY KEY (shipment_id),
                           FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

-- ================================================================
-- SEED DATA
-- ================================================================

-- Roles
INSERT INTO roles (role_name) VALUES ('ROLE_CUSTOMER'), ('ROLE_ADMIN'), ('ROLE_PHARMACIST');

-- Admin (password: Admin@123)
INSERT INTO users (user_name, password, full_name, email, phone, role_id)
SELECT 'admin',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
       'Quản trị viên', 'admin@pharmacy.vn', '0900000000', role_id
FROM roles WHERE role_name = 'ROLE_ADMIN';

-- Manufacturers
INSERT INTO manufacturers (name, country) VALUES
                                              ('Ipsen Pharma',     'Pháp'),
                                              ('Bayer',            'Đức'),
                                              ('Janssen',          'Bỉ'),
                                              ('Pfizer',           'Mỹ'),
                                              ('Blackmores',       'Úc'),
                                              ('Dược Hậu Giang',   'Việt Nam'),
                                              ('Traphaco',         'Việt Nam'),
                                              ('Omron',            'Nhật Bản');

-- Categories
INSERT INTO categories (name, slug) VALUES
                                        ('Thuốc tiêu hoá',        'thuoc-tieu-hoa'),
                                        ('Vitamin & Khoáng chất', 'vitamin-khoang-chat'),
                                        ('Thuốc hô hấp',          'thuoc-ho-hap'),
                                        ('Thuốc tim mạch',        'thuoc-tim-mach'),
                                        ('Dược mỹ phẩm',          'duoc-my-pham'),
                                        ('Thiết bị y tế',         'thiet-bi-y-te');

-- Medicines
INSERT INTO medicines (name, slug, description, price, unit, category_id, manufacturer_id, status)
VALUES
    ('Smecta 3g', 'smecta-3g',
     'Điều trị tiêu chảy cấp và mãn tính', 85000, 'Hộp 30 gói',
     (SELECT category_id FROM categories WHERE slug = 'thuoc-tieu-hoa'),
     (SELECT manufacturer_id FROM manufacturers WHERE name = 'Ipsen Pharma'), 'ACTIVE'),

    ('Motilium-M 10mg', 'motilium-m-10mg',
     'Điều trị buồn nôn, nôn, đầy bụng', 120000, 'Hộp 30 viên',
     (SELECT category_id FROM categories WHERE slug = 'thuoc-tieu-hoa'),
     (SELECT manufacturer_id FROM manufacturers WHERE name = 'Janssen'), 'ACTIVE'),

    ('Vitamin C 1000mg Redoxon', 'vitamin-c-1000mg-redoxon',
     'Bổ sung vitamin C, tăng sức đề kháng', 180000, 'Hộp 10 ống sủi',
     (SELECT category_id FROM categories WHERE slug = 'vitamin-khoang-chat'),
     (SELECT manufacturer_id FROM manufacturers WHERE name = 'Bayer'), 'ACTIVE'),

    ('Centrum Silver', 'centrum-silver',
     'Vitamin tổng hợp cho người trên 50 tuổi', 450000, 'Hộp 30 viên',
     (SELECT category_id FROM categories WHERE slug = 'vitamin-khoang-chat'),
     (SELECT manufacturer_id FROM manufacturers WHERE name = 'Pfizer'), 'ACTIVE'),

    ('Blackmores Fish Oil 1000mg', 'blackmores-fish-oil-1000mg',
     'Omega-3 hỗ trợ tim mạch và não bộ', 320000, 'Hộp 60 viên',
     (SELECT category_id FROM categories WHERE slug = 'vitamin-khoang-chat'),
     (SELECT manufacturer_id FROM manufacturers WHERE name = 'Blackmores'), 'ACTIVE'),

    ('Tiffy Forte', 'tiffy-forte',
     'Điều trị cảm cúm, sổ mũi, nghẹt mũi', 45000, 'Hộp 24 viên',
     (SELECT category_id FROM categories WHERE slug = 'thuoc-ho-hap'),
     (SELECT manufacturer_id FROM manufacturers WHERE name = 'Dược Hậu Giang'), 'ACTIVE'),

    ('Broncol 5mg', 'broncol-5mg',
     'Điều trị ho, long đờm', 78000, 'Hộp 20 viên',
     (SELECT category_id FROM categories WHERE slug = 'thuoc-ho-hap'),
     (SELECT manufacturer_id FROM manufacturers WHERE name = 'Dược Hậu Giang'), 'ACTIVE'),

    ('Máy đo huyết áp Omron HEM-7120', 'may-do-huyet-ap-omron-7120',
     'Đo huyết áp bắp tay tự động, kết quả chính xác', 890000, 'Cái',
     (SELECT category_id FROM categories WHERE slug = 'thiet-bi-y-te'),
     (SELECT manufacturer_id FROM manufacturers WHERE name = 'Omron'), 'ACTIVE');

-- Inventory (tồn kho ban đầu)
INSERT INTO inventory (medicine_id, quantity)
SELECT medicine_id, 150 FROM medicines WHERE slug = 'smecta-3g';
INSERT INTO inventory (medicine_id, quantity)
SELECT medicine_id, 80  FROM medicines WHERE slug = 'motilium-m-10mg';
INSERT INTO inventory (medicine_id, quantity)
SELECT medicine_id, 200 FROM medicines WHERE slug = 'vitamin-c-1000mg-redoxon';
INSERT INTO inventory (medicine_id, quantity)
SELECT medicine_id, 60  FROM medicines WHERE slug = 'centrum-silver';
INSERT INTO inventory (medicine_id, quantity)
SELECT medicine_id, 90  FROM medicines WHERE slug = 'blackmores-fish-oil-1000mg';
INSERT INTO inventory (medicine_id, quantity)
SELECT medicine_id, 300 FROM medicines WHERE slug = 'tiffy-forte';
INSERT INTO inventory (medicine_id, quantity)
SELECT medicine_id, 180 FROM medicines WHERE slug = 'broncol-5mg';
INSERT INTO inventory (medicine_id, quantity)
SELECT medicine_id, 25  FROM medicines WHERE slug = 'may-do-huyet-ap-omron-7120';
