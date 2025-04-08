INSERT IGNORE INTO category (id, name, parent_id)
VALUES (1, 'Electronics', NULL),
       (2, 'Laptops', 1),
       (3, 'Desktops', 1),
       (4, 'Smartphones', 1),
       (5, 'Smart TVs', 1),
       (6, 'Gaming Consoles', 1),
       (7, 'Printers, Scanners & Fax Machines', 1),
       (8, 'Clothing', NULL),
       (9, 'Men\'s Fashion', 8),
       (10, 'Women\'s Fashion', 8),
       (11, 'Home & Kitchen', NULL),
       (12, 'Entertainment', NULL),
       (13, 'TV & Movies', 12),
       (14, 'Books', 12),
       (15, 'Videogames', 12),
       (16, 'Cameras', 1);

-- TODO: More categories and products

INSERT IGNORE INTO role(id, name)
VALUES (1, 'ADMIN'),
       (2, 'USER');

INSERT IGNORE INTO user(id, name, email, password, home_country, image_path, enabled, mfa_enabled, secret,
                        registered_at)
VALUES (1, 'Theodore Meras', 'tmeras@yahoo.gr', '$2a$10$h2UMOIQGcBEjM0Dc3if4BuRBLzKKhnGy8i.vesnGwphl0BmTI/yMi',
        'Greece', './uploads/flyway-user-images/user_image.png', true, false, null, CURDATE()),
       (2, 'Edmund Smith', 'admin@yahoo.gr', '$2a$10$h2UMOIQGcBEjM0Dc3if4BuRBLzKKhnGy8i.vesnGwphl0BmTI/yMi',
        'United Kingdom', './uploads/flyway-user-images/admin_image.png', true, false, null, CURDATE()),
       (3, 'Mary Cole', 'mary@gmail.com', '$2a$10$h2UMOIQGcBEjM0Dc3if4BuRBLzKKhnGy8i.vesnGwphl0BmTI/yMi',
        'United Kingdom', './uploads/flyway-user-images/user_image_2.png', true, false, null, CURDATE());

INSERT IGNORE INTO user_roles(user_id, roles_id)
VALUES (1, 2),
       (2, 1),
       (3, 2);

INSERT IGNORE INTO address(id, country, street, state, city, postal_code, main, deleted, address_type, user_id)
VALUES (1, 'Greece', 'Ermou Street', 'Attica', 'Athens', '10563',
        true, false, 'HOME', 1),
       (2, 'UK', 'Mappin Street', 'South Yorkshire', 'Sheffield', 'S1 4DT',
        true, false, 'WORK', 2),
       (3, 'UK', 'Oxford Road', 'Greater Manchester', 'Manchester', 'M13 9PL',
        true, false, 'HOME', 3);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (1, 'Iphone 16 Pro Max', 'A brand new Iphone 16 Pro Max. Used for <2 hours.', 200, NULL,
        'LIKE_NEW', 1, false, 4, 1);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (1, './uploads/flyway-product-images/iphone_16_pro_max.png', true, 1);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (2, 'MacBook Air 13\'\' M1', 'A brand new MacBook Air with M1 chip. Still sealed.', 1300,1500,
        'NEW', 1, false, 2, 1);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (2, './uploads/flyway-product-images/macbook_air_M1.png', true, 2);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (3, 'Samsung Galaxy S22', 'A brand new Samsung Galaxy S22 with no scratches. Sealed in box.', 750, NULL,
        'NEW', 5, false, 4, 1);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (3, './uploads/flyway-product-images/s22_1.png', true, 3),
       (4, './uploads/flyway-product-images/s22_2.png', false, 3),
       (5, './uploads/flyway-product-images/s22_3.png', false, 3);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (4, 'Sony WH-1000XM4', 'Top-rated noise-canceling headphones, like new condition.', 350, NULL,
        'LIKE_NEW', 10, false, 1, 1);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (6, './uploads/flyway-product-images/xm4_1.png', true, 4),
       (7, './uploads/flyway-product-images/xm4_2.png', false, 4),
       (8, './uploads/flyway-product-images/xm4_3.png', false, 4);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (5, 'Apple iPad Pro 12.9\'\'', 'Brand new iPad Pro, sealed.', 1100, NULL,
        'NEW', 7, false, 1, 1);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (9, './uploads/flyway-product-images/ipad_1.png', true, 5),
       (10, './uploads/flyway-product-images/ipad_2.png', false, 5);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (6, 'LG 55\'\' OLED TV',
        'A high-quality OLED TV with amazing picture quality. Used for 6 months, has some dead pixels.', 1300, 1200,
        'DAMAGED', 1, false, 5, 2);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (11, './uploads/flyway-product-images/lg_oled_1.png', true, 6),
       (12, './uploads/flyway-product-images/lg_oled_2.png', false, 6);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (7, 'Bose QuietComfort', 'Amazing noise-canceling headphones in mint condition. Comes with carrying pouch.', 300,
        NULL,
        'LIKE_NEW', 1, false, 1, 2);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (13, './uploads/flyway-product-images/bose_1.png', true, 7),
       (14, './uploads/flyway-product-images/bose_2.png', false, 7),
       (15, './uploads/flyway-product-images/bose_3.png', false, 7);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (8, 'Nike T-shirt', 'A comfortable cotton T-shirt for casual wear.', 25, NULL,
        'NEW', 50, false, 8, 2);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (16, './uploads/flyway-product-images/nike_shirt_1.png', true, 8);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (9, 'Levi\'s Jeans', 'Stylish denim jeans, good condition.', 60, NULL,
        'FAIR', 30, false, 9, 2);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (17, './uploads/flyway-product-images/levis_jeans_1.png', true, 9),
       (18, './uploads/flyway-product-images/levis_jeans_2.png', false, 9);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (10, 'Sony PlayStation 5 Slim', 'Brand new sealed PS5 Slim, with 1 controller.', 580, 600,
        'NEW', 20, false, 6, 2);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (19, './uploads/flyway-product-images/ps5_slim_1.png', true, 10),
       (20, './uploads/flyway-product-images/ps5_slim_2.png', false, 10);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (11, 'Canon EOS 5D Mark IV', 'Used Canon DSLR camera with 2 lenses. Great condition.', 1000, NULL,
        'LIKE_NEW', 3, false, 16, 2);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (21, './uploads/flyway-product-images/canon_1.png', true, 11),
       (22, './uploads/flyway-product-images/canon_2.png', false, 11);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (12, 'Nintendo Switch OLED', 'Brand new Nintendo Switch OLED model. Sealed.', 350, NULL,
        'NEW', 8, false, 6, 2);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (23, './uploads/flyway-product-images/switch_1.png', true, 12),
       (24, './uploads/flyway-product-images/switch_2.png', false, 12),
       (25, './uploads/flyway-product-images/switch_3.png', false, 12);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (13, 'Harry Potter Book Set', 'A complete set of all 7 Harry Potter books in great condition.', 150, NULL,
        'LIKE_NEW', 15, false, 14, 3);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (26, './uploads/flyway-product-images/harry_1.png', true, 13),
       (27, './uploads/flyway-product-images/harry_2.png', false, 13);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (14, 'The Lord of the Rings Trilogy', 'A 3-book set of The Lord of the Rings in new condition.', 60, NULL,
        'NEW', 10, false, 13, 3);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (28, './uploads/flyway-product-images/lotr_1.png', true, 14);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (15, 'Assassin\'s Creed Valhalla PS4', 'Brand new, sealed copy of Assassin\'s Creed Valhalla for PlayStation 5.',
        60, NULL,
        'NEW', 12, false, 15, 3);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (29, './uploads/flyway-product-images/ac_1.jpeg', true, 15);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (16, 'Xbox Series X 1TB', 'Used Xbox Series X console with damaged cover', 450, 500,
        'DAMAGED', 5, false, 6, 3);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (30, './uploads/flyway-product-images/xbox_1.jpeg', true, 16),
       (31, './uploads/flyway-product-images/xbox_2.jpeg', false, 16);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id)
VALUES (17, 'The Witcher 3: Wild Hunt GOTY Edition PS4',
        'Used but in great condition, a popular open-world fantasy RPG for PS4.', 25, NULL,
        'LIKE_NEW', 8, false, 15, 3);
INSERT IGNORE INTO product_image(id, file_path, displayed, product_id)
VALUES (32, './uploads/flyway-product-images/witcher3_1.jpeg', true, 17),
       (33, './uploads/flyway-product-images/witcher3_2.jpeg', false, 17);

INSERT IGNORE INTO customer_order(id, placed_at, payment_method, billing_address_id, delivery_address_id, buyer_id)
VALUES (1, NOW(), 'CASH', 3, 3, 3);
INSERT IGNORE INTO order_item(id, product_id, product_quantity, order_id)
VALUES (1, 8, 2, 1),
       (2, 9, 3, 1);

INSERT IGNORE INTO customer_order(id, placed_at, payment_method, billing_address_id, delivery_address_id, buyer_id)
VALUES (2, NOW(), 'CARD', 1, 1, 1);
INSERT IGNORE INTO order_item(id, product_id, product_quantity, order_id)
VALUES (3, 11, 1, 2);

INSERT IGNORE INTO customer_order(id, placed_at, payment_method, billing_address_id, delivery_address_id, buyer_id)
VALUES (3, NOW(), 'CARD', 1, 1, 1);
INSERT IGNORE INTO order_item(id, product_id, product_quantity, order_id)
VALUES (4, 12, 1, 3),
       (5, 13, 1, 3);

INSERT IGNORE INTO customer_order(id, placed_at, payment_method, billing_address_id, delivery_address_id, buyer_id)
VALUES (4, NOW(), 'CARD', 2, 2, 2);
INSERT IGNORE INTO order_item(id, product_id, product_quantity, order_id)
VALUES (6, 15, 1, 4),
       (7, 16, 2, 4);

INSERT IGNORE INTO customer_order(id, placed_at, payment_method, billing_address_id, delivery_address_id, buyer_id)
VALUES (5, NOW(), 'CARD', 3, 3, 3);
INSERT IGNORE INTO order_item(id, product_id, product_quantity, order_id)
VALUES (8, 4, 2, 5),
       (9, 3, 1, 5);