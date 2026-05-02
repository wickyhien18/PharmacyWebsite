CREATE TABLE `roles` (
                         `role_id` BIGINT NOT NULL AUTO_INCREMENT,
                         `role_name` varchar(255) DEFAULT NULL,
                         `description` varchar(255) DEFAULT NULL,
                         PRIMARY KEY (`role_id`)
);
CREATE TABLE `users` (
                         `user_id` BIGINT NOT NULL AUTO_INCREMENT,
                         `user_name` varchar(255) NOT NULL,
                         `password` varchar(255) DEFAULT NULL,
                         `role_id` BIGINT DEFAULT NULL,
                         `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                         `last_activity` datetime DEFAULT CURRENT_TIMESTAMP,
                         `is_active` tinyint(1) DEFAULT 1,
                         PRIMARY KEY (`user_id`),
                         UNIQUE KEY `uu_user_name` (`user_name`),
                         KEY `fk_user_role` (`role_id`),
                         CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`) ON DELETE SET NULL
);

CREATE TABLE `categories` (
                              `category_id` BIGINT NOT NULL AUTO_INCREMENT,
                              `category_name` varchar(255) NOT NULL,
                              `description` varchar(255) DEFAULT NULL,
                              PRIMARY KEY (`category_id`)
);

CREATE TABLE `manufacturers` (
                                 `manufacturer_id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `manufacturer_name` varchar(255) NOT NULL,
                                 `country` varchar(255) DEFAULT NULL,
                                 PRIMARY KEY (`manufacturer_id`)
);

CREATE TABLE `medicines` (
                             `medicine_id` BIGINT NOT NULL AUTO_INCREMENT,
                             `medicine_name` varchar(255) NOT NULL,
                             `medicine_image` varchar(255) DEFAULT NULL,
                             `description` TEXT DEFAULT NULL,
                             `price` decimal(15,2) NOT NULL,
                             `quantity` int DEFAULT NULL,
                             `manufacturer_id` BIGINT DEFAULT NULL,
                             `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                             `category_id` BIGINT DEFAULT NULL,
                             PRIMARY KEY (`medicine_id`),
                             KEY `fk_medicine_manufacturer` (`manufacturer_id`),
                             KEY `fk_medicines_category` (`category_id`),
                             CONSTRAINT `fk_medicine_manufacturer` FOREIGN KEY (`manufacturer_id`)
                                 REFERENCES `manufacturers` (`manufacturer_id`) ON DELETE SET NULL,
                             CONSTRAINT `fk_medicines_category` FOREIGN KEY (`category_id`)
                                 REFERENCES `categories` (`category_id`) ON DELETE SET NULL ON UPDATE RESTRICT
);

CREATE TABLE `carts` (
                         `cart_id` BIGINT NOT NULL AUTO_INCREMENT,
                         `user_id` BIGINT DEFAULT NULL,
                         `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (`cart_id`),
                         KEY `fk_cart_user` (`user_id`),
                         CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
);

CREATE TABLE `cart_item` (
                             `Id` BIGINT NOT NULL AUTO_INCREMENT,
                             `cart_id` BIGINT DEFAULT NULL,
                             `medicine_id` BIGINT DEFAULT NULL,
                             `quantity` int DEFAULT NULL,
                             PRIMARY KEY (`Id`),
                             KEY `fk_cart_medicine` (`cart_id`),
                             KEY `fk_medicine_cart` (`medicine_id`),
                             CONSTRAINT `fk_cart_medicine` FOREIGN KEY (`cart_id`)
                                 REFERENCES `carts` (`cart_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                             CONSTRAINT `fk_medicine_cart` FOREIGN KEY (`medicine_id`)
                                 REFERENCES `medicines` (`medicine_id`) ON DELETE CASCADE
);

CREATE TABLE `orders` (
                          `order_id` BIGINT NOT NULL AUTO_INCREMENT,
                          `user_id` BIGINT DEFAULT NULL,
                          `total_price` decimal(15,2) NOT NULL,
                          `status` varchar(255) DEFAULT NULL,
                          `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`order_id`),
                          KEY `fk_order_user` (`user_id`),
                          CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`)
                              REFERENCES `users` (`user_id`) ON DELETE SET NULL
);

CREATE TABLE `order_item` (
                              `Id` BIGINT NOT NULL AUTO_INCREMENT,
                              `order_id` BIGINT DEFAULT NULL,
                              `medicine_id` BIGINT DEFAULT NULL,
                              `price` decimal(15,2) DEFAULT NULL,
                              `quantity` int DEFAULT NULL,
                              PRIMARY KEY (`Id`),
                              KEY `fk_medicine_order` (`medicine_id`),
                              KEY `fk_order_medicine` (`order_id`),
                              CONSTRAINT `fk_medicine_order` FOREIGN KEY (`medicine_id`)
                                  REFERENCES `medicines` (`medicine_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                              CONSTRAINT `fk_order_medicine` FOREIGN KEY (`order_id`)
                                  REFERENCES `orders` (`order_id`) ON DELETE CASCADE
);


CREATE TABLE `payments` (
                            `payment_id` BIGINT NOT NULL AUTO_INCREMENT,
                            `order_id` BIGINT DEFAULT NULL,
                            `amount` decimal(15,2) NOT NULL,
                            `payment_method` varchar(255) DEFAULT NULL,
                            `status` varchar(255) DEFAULT NULL,
                            `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`payment_id`),
                            KEY `fk_payment_order` (`order_id`),
                            CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`)
                                REFERENCES `orders` (`order_id`) ON DELETE CASCADE
);


CREATE TABLE `refresh_token` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `token` varchar(255) DEFAULT NULL,
                                 `user_id` BIGINT NOT NULL,
                                 `expire_at` datetime DEFAULT NULL,
                                 `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `token` (`token`),
                                 KEY `fk_refresh_token_user` (`user_id`),
                                 CONSTRAINT `fk_refresh_token_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);


