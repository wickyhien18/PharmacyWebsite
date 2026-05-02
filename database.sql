CREATE TABLE roles (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    role_id BIGINT,

    is_active TINYINT(1) DEFAULT 1,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_activity TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,

    CONSTRAINT fk_user_role 
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE SET NULL
);

CREATE TABLE categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE manufacturers (
    manufacturer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(255)
);

CREATE TABLE medicines (
    medicine_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    slug VARCHAR(520) NOT NULL UNIQUE,
    image VARCHAR(500),
    description TEXT,

    price DECIMAL(15,2) NOT NULL,
    unit VARCHAR(50) DEFAULT 'Pillbox',

    category_id BIGINT,
    manufacturer_id BIGINT,

    status ENUM('ACTIVE','INACTIVE','OUT_OF_STOCK') DEFAULT 'ACTIVE',

    expire_date DATE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    INDEX idx_category (category_id),
    INDEX idx_manufacturer (manufacturer_id),

    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL,
    FOREIGN KEY (manufacturer_id) REFERENCES manufacturers(manufacturer_id) ON DELETE SET NULL
);

CREATE TABLE inventory (
    inventory_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,

    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_inventory_medicine (medicine_id),

    FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) ON DELETE CASCADE
);

CREATE TABLE carts (
    cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,

    UNIQUE KEY uk_cart_medicine (cart_id, medicine_id),

    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) ON DELETE CASCADE
);

CREATE TABLE orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,

    order_code VARCHAR(100) NOT NULL UNIQUE,

    total_price DECIMAL(15,2) NOT NULL,

    order_status ENUM('PENDING','CONFIRMED','SHIPPING','DELIVERED','CANCELLED') 
        DEFAULT 'PENDING',

    payment_status ENUM('PENDING','PAID','FAILED') DEFAULT 'PENDING',

    shipping_address VARCHAR(500),
    note TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_status (order_status)
);

CREATE TABLE order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    medicine_id BIGINT,

    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,

    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) ON DELETE SET NULL,

    INDEX idx_order (order_id)
);

CREATE TABLE shipments (
    shipment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,

    tracking_code VARCHAR(100),
    carrier VARCHAR(100),

    status ENUM('PENDING','SHIPPING','DELIVERED','FAILED'),

    shipped_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,

    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,

    expire_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE inventory_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    medicine_id BIGINT NOT NULL,

    change_type ENUM('IMPORT','EXPORT','ADJUST') NOT NULL,

    quantity INT NOT NULL,
    previous_quantity INT NOT NULL,
    new_quantity INT NOT NULL,

    reference_id BIGINT NULL, 
    -- liên kết order_id nếu là bán hàng

    note VARCHAR(500),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) ON DELETE CASCADE,

    INDEX idx_medicine (medicine_id),
    INDEX idx_type (change_type)
);

CREATE TABLE payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    order_id BIGINT NOT NULL,

    payment_method ENUM('COD','VNPAY','MOMO') NOT NULL,

    amount DECIMAL(15,2) NOT NULL,

    transaction_code VARCHAR(255), 
    -- mã từ cổng thanh toán

    status ENUM('PENDING','SUCCESS','FAILED') DEFAULT 'PENDING',

    paid_at TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,

    INDEX idx_order (order_id),
    INDEX idx_status (status)
);
