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
       (11, 'Home', NULL),
       (12, 'Entertainment', NULL),
       (13, 'TV & Movies', 12),
       (14, 'Books', 12),
       (15, 'Videogames', 12),
       (16, 'Cameras', 1),
       (17, 'Furniture', 11),
       (18, 'Kitchen', 11),
       (19, 'Computer Accessories', 1);

INSERT IGNORE INTO role(id, name)
VALUES (1, 'ADMIN'),
       (2, 'USER');

INSERT IGNORE INTO user(id, name, email, password, home_country, image_path, is_enabled, is_mfa_enabled, secret,
                        registered_at)
VALUES (1, 'Theodore Meras', 'tmeras@yahoo.gr', '$2a$10$uu9TwmhE0hMAPAmTNFsPjes8lUuw1RCC5iZPuPlk7aU.xWAYmJyDy',
        'Greece', './uploads/flyway-user-images/user_image.png', true, false, null, CURDATE()),
       (2, 'Edmund Smith', 'admin@yahoo.gr', '$2a$10$uu9TwmhE0hMAPAmTNFsPjes8lUuw1RCC5iZPuPlk7aU.xWAYmJyDy',
        'United Kingdom', './uploads/flyway-user-images/admin_image.png', true, false, null, CURDATE()),
       (3, 'Mary Cole', 'mary@gmail.com', '$2a$10$uu9TwmhE0hMAPAmTNFsPjes8lUuw1RCC5iZPuPlk7aU.xWAYmJyDy',
        'United Kingdom', './uploads/flyway-user-images/user_image_2.png', true, false, null, CURDATE()),
        (4, 'John Doe', 'john@gmail.com', '$2a$10$uu9TwmhE0hMAPAmTNFsPjes8lUuw1RCC5iZPuPlk7aU.xWAYmJyDy',
        'United Kingdom', null, true, false, null, CURDATE());

INSERT IGNORE INTO user_role(user_id, role_id)
VALUES (1, 2),
       (2, 1),
       (3, 2),
       (4, 1);

INSERT IGNORE INTO address(id, country, street, state, city, postal_code, is_main, address_type, user_id, name, phone_number)
VALUES (1, 'Greece', 'Ermou Street', 'Attica', 'Athens', '10563',
        true, 'HOME', 1, 'Theodore Meras', '+302101234567'),
       (2, 'United Kingdom', 'Mappin Street', 'South Yorkshire', 'Sheffield', 'S1 4DT',
        true, 'WORK', 2, 'Edmund Smith', '+441234567890'),
       (3, 'United Kingdom', 'Oxford Road', 'Greater Manchester', 'Manchester', 'M13 9PL',
        true, 'HOME', 3, 'Mary Cole', '+441612345678'),
        (4, 'United Kingdom', '221B Baker Street', 'Greater London', 'London', 'NW1 6XE',
        true, 'HOME', 4, 'John Doe', '+447911123456');

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (1, 'Iphone 16 Pro Max', 'A brand new Iphone 16 Pro Max. Used for <2 hours.', 500, NULL,
        'LIKE_NEW', 1, false, 4, 1, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES (1, './uploads/flyway-product-images/iphone_16_pro_max.png', 'iphone_16_pro_max.png', 'image/png', 1);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (2, 'MacBook Air 13\'\' M1', 'A brand new MacBook Air with M1 chip. Still sealed.', 1300,1500,
        'NEW', 1, false, 2, 1, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES (2, './uploads/flyway-product-images/macbook_air_M1.png', 'macbook_air_M1.png', 'image/png', 2);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (3, 'Samsung Galaxy S22', 'A brand new Samsung Galaxy S22 with no scratches. Sealed in box.', 750, NULL,
        'NEW', 5, false, 4, 1, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(3, './uploads/flyway-product-images/s22_1.png', 's22_1.png', 'image/png', 3),
(4, './uploads/flyway-product-images/s22_2.png', 's22_2.png', 'image/png', 3),
(5, './uploads/flyway-product-images/s22_3.png', 's22_3.png', 'image/png', 3);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (4, 'Sony WH-1000XM4', 'Top-rated noise-canceling headphones, like new condition.', 350, NULL,
        'LIKE_NEW', 10, false, 19, 1, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(6, './uploads/flyway-product-images/xm4_1.png', 'xm4_1.png', 'image/png', 4),
(7, './uploads/flyway-product-images/xm4_2.png', 'xm4_2.png', 'image/png', 4),
(8, './uploads/flyway-product-images/xm4_3.png', 'xm4_3.png', 'image/png', 4);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (5, 'Apple iPad Pro 12.9\'\'', 'Brand new iPad Pro, sealed.', 1100, NULL,
        'NEW', 7, false, 1, 1, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(9, './uploads/flyway-product-images/ipad_1.png', 'ipad_1.png', 'image/png', 5),
(10, './uploads/flyway-product-images/ipad_2.png', 'ipad_2.png', 'image/png', 5);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (6, 'LG 55\'\' OLED TV',
        'A high-quality OLED TV with amazing picture quality. Used for 6 months, has some dead pixels.', 1300, 1200,
        'DAMAGED', 1, false, 5, 2, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(11, './uploads/flyway-product-images/lg_oled_1.png', 'lg_oled_1.png', 'image/png', 6),
(12, './uploads/flyway-product-images/lg_oled_2.png', 'lg_oled_2.png', 'image/png', 6);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (7, 'Bose QuietComfort', 'Amazing noise-canceling headphones in mint condition. Comes with carrying pouch.', 300,
        NULL, 'LIKE_NEW', 1, false, 19, 2, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(13, './uploads/flyway-product-images/bose_1.png', 'bose_1.png', 'image/png', 7),
(14, './uploads/flyway-product-images/bose_2.png', 'bose_2.png', 'image/png', 7),
(15, './uploads/flyway-product-images/bose_3.png', 'bose_3.png', 'image/png', 7);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (8, 'Nike T-shirt', 'A comfortable cotton T-shirt for casual wear.', 25, NULL,
        'NEW', 50, false, 8, 2, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES (16, './uploads/flyway-product-images/nike_shirt_1.png', 'nike_shirt_1.png', 'image/png', 8);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (9, 'Levi\'s Jeans', 'Stylish denim jeans, good condition.', 60, NULL,
        'FAIR', 30, false, 9, 2, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(17, './uploads/flyway-product-images/levis_jeans_1.png', 'levis_jeans_1.png', 'image/png', 9),
(18, './uploads/flyway-product-images/levis_jeans_2.png', 'levis_jeans_2.png', 'image/png', 9);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (10, 'Sony PlayStation 5 Slim', 'Brand new sealed PS5 Slim, with 1 controller.', 580, 600,
        'NEW', 20, false, 6, 2, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(19, './uploads/flyway-product-images/ps5_slim_1.png', 'ps5_slim_1.png', 'image/png', 10),
(20, './uploads/flyway-product-images/ps5_slim_2.png', 'ps5_slim_2.png', 'image/png', 10);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (11, 'Canon EOS 5D Mark IV', 'Used Canon DSLR camera with 2 lenses. Great condition.', 1000, NULL,
        'LIKE_NEW', 3, false, 16, 2, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(21, './uploads/flyway-product-images/canon_1.png', 'canon_1.png', 'image/png', 11),
(22, './uploads/flyway-product-images/canon_2.png', 'canon_2.png', 'image/png', 11);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (12, 'Nintendo Switch OLED', 'Brand new Nintendo Switch OLED model. Sealed.', 350, NULL,
        'NEW', 8, false, 6, 2, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(23, './uploads/flyway-product-images/switch_1.png', 'switch_1.png', 'image/png', 12),
(24, './uploads/flyway-product-images/switch_2.png', 'switch_2.png', 'image/png', 12),
(25, './uploads/flyway-product-images/switch_3.png', 'switch_3.png', 'image/png', 12);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (13, 'Harry Potter Book Set', 'A complete set of all 7 Harry Potter books in great condition.', 150, NULL,
        'LIKE_NEW', 15, false, 14, 3, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(26, './uploads/flyway-product-images/harry_1.png', 'harry_1.png', 'image/png', 13),
(27, './uploads/flyway-product-images/harry_2.png', 'harry_2.png', 'image/png', 13);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (14, 'The Lord of the Rings Trilogy', 'A 3-book set of The Lord of the Rings in new condition.', 60, NULL,
        'NEW', 10, false, 13, 3, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES (28, './uploads/flyway-product-images/lotr_1.png', 'lotr_1.png', 'image/png', 14);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (15, 'Assassin\'s Creed Valhalla PS4', 'Brand new, sealed copy of Assassin\'s Creed Valhalla for PlayStation 5.',
        60, NULL, 'NEW', 12, false, 15, 3, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES (29, './uploads/flyway-product-images/ac_1.jpeg', 'ac_1.jpeg', 'image/jpeg', 15);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (16, 'Xbox Series X 1TB', 'Used Xbox Series X console with damaged cover', 450, 500,
        'DAMAGED', 5, false, 6, 3, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(30, './uploads/flyway-product-images/xbox_1.jpeg', 'xbox_1.jpeg', 'image/jpeg', 16),
(31, './uploads/flyway-product-images/xbox_2.jpeg', 'xbox_2.jpeg', 'image/jpeg', 16);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (17, 'The Witcher 3: Wild Hunt GOTY Edition PS4',
        'Used but in great condition, a popular open-world fantasy RPG for PS4.', 25, NULL,
        'LIKE_NEW', 8, false, 15, 3, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(32, './uploads/flyway-product-images/witcher3_1.jpeg', 'witcher3_1.jpeg', 'image/jpeg', 17),
(33, './uploads/flyway-product-images/witcher3_2.jpeg', 'witcher3_2.jpeg', 'image/jpeg', 17);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (18, 'Logitech M220 Wireless Mouse',
        'Silent, compact wireless mouse with long battery life and smooth tracking.', 100, NULL,
        'NEW', 8, false, 19, 4, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(34, './uploads/flyway-product-images/m220_1.png', 'm220_1.png', 'image/png', 18);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (19, 'White Double Wardrobe',
        'Spacious double wardrobe with clean white finish and ample storage.', 250, 300,
        'LIKE_NEW', 1, false, 17, 4, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(35, './uploads/flyway-product-images/wardrobe_1.png', 'wardrobe_1.png', 'image/png', 19),
(36, './uploads/flyway-product-images/wardrobe_2.png', 'wardrobe_2.png', 'image/png', 19);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (20, 'Kitchen Table',
        'Simple and sturdy kitchen table, seats four comfortably.', 30, NULL,
        'FAIR', 3, false, 18, 4, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(37, './uploads/flyway-product-images/table_1.png', 'table_1.png', 'image/png', 20);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (21, 'Stainless Steel Knife Set',
        'Durable and sharp stainless steel knives for everyday kitchen use.', 150, NULL,
        'NEW', 8, false, 18, 4, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(38, './uploads/flyway-product-images/knife_1.png', 'table_1.png', 'image/png', 21),
(39, './uploads/flyway-product-images/knife_2.png', 'table_2.png', 'image/png', 21);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (22, 'Tesla Minibar',
        'Compact and energy-efficient minibar, ideal for bedrooms or small offices.', 500, NULL,
        'NEW', 3, false, 18, 4, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(40, './uploads/flyway-product-images/tesla_minibar_1.png', 'tesla_minibar_1.png', 'image/png', 22);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (23, 'Epson L6270 All-in-One Printer',
        'Compact inkjet printer with wireless, print, scan, and copy capabilities.', 300, 600,
        'FAIR', 2, false, 7, 4, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(41, './uploads/flyway-product-images/epson_l6270_1.png', 'epson_l6270_1.png', 'image/png', 23),
(42, './uploads/flyway-product-images/epson_l6270_2.png', 'epson_l6270_2.png', 'image/png', 23),
(43, './uploads/flyway-product-images/epson_l6270_3.png', 'epson_l6270_3.png', 'image/png', 23);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (24, 'HP Officejet Pro 9730e AiO A3 Colour All In One Inkjet Printer',
        'High-performance all-in-one printer with A3 color printing, scanning, and copying.', 500, NULL,
        'LIKE_NEW', 5, false, 7, 3, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(44, './uploads/flyway-product-images/officejet_1.png', 'officejet_1.png', 'image/png', 24);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (25, 'Brown Straw Hat',
        'Lightweight and breathable straw hat ideal for sunny days and outdoor events.', 15, NULL,
        'NEW', 15, false, 10, 3, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(45, './uploads/flyway-product-images/hat_1.png', 'hat_1.png', 'image/png', 25);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (26, 'Tamaris Leather Black Heels with Strap',
        'Elegant black leather heels with ankle strap, perfect for formal occasions.', 70, NULL,
        'NEW', 10, false, 10, 3, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(46, './uploads/flyway-product-images/heels_1.png', 'heels_1.png', 'image/png', 26),
(47, './uploads/flyway-product-images/heels_2.png', 'heels_2.png', 'image/png', 26);

INSERT IGNORE INTO product(id, name, description, price, previous_price, product_condition,
                           available_quantity, is_deleted, category_id, seller_id, listed_at)
VALUES (27, 'Vengeance Tornado-X8 Gaming Desktop PC (Ryzen 7-5700/32GB DDR4/1TB SSD/Radeon RX 6400/No OS)',
        'High-performance gaming PC with Ryzen 7 processor, 32GB RAM, 1TB SSD, and Radeon RX 6400 graphics—ideal for smooth 1080p gaming and multitasking. No operating system included.',
         700, NULL, 'LIKE_NEW', 4, false, 3, 2, UTC_TIMESTAMP());
INSERT IGNORE INTO product_image(id, image_path, name, type, product_id)
VALUES
(48, './uploads/flyway-product-images/pc.png', 'pc.png', 'image/png', 27);

INSERT IGNORE INTO customer_order(id, placed_at, payment_method, billing_address,
                                 delivery_address, buyer_id, status, stripe_checkout_id)
VALUES (1, UTC_TIMESTAMP(), 'card', "Mary Cole, Oxford Road, Greater Manchester, Manchester, M13 9PL, United Kingdom, +441612345678",
        "Mary Cole, Oxford Road, Greater Manchester, Manchester, M13 9PL, United Kingdom, +441612345678", 3, "PAID", NULL);
INSERT IGNORE INTO order_item(id, product_id, product_quantity, order_id, status, product_name, product_price,
                             product_condition, product_image_path, product_seller_id)
VALUES (1, 8, 2, 1, 'SHIPPED', 'Nike T-shirt', 25, 'NEW', './uploads/flyway-order-item-images/nike_shirt_1.png', 2),
       (2, 9, 3, 1, 'DELIVERED', 'Levi\'s Jeans', 60, 'FAIR', './uploads/flyway-order-item-images/levis_jeans_1.png', 2);

INSERT IGNORE INTO customer_order(id, placed_at, payment_method, billing_address,
                                 delivery_address, buyer_id, status, stripe_checkout_id)
VALUES (2, UTC_TIMESTAMP(), 'card', "Theodore Meras, Ermou Street, Attica, Athens, 10563, Greece, +302101234567",
        "Theodore Meras, Ermou Street, Attica, Athens, 10563, Greece, +302101234567", 1, "PAID", NULL);
INSERT IGNORE INTO order_item(id, product_id, product_quantity, order_id, status, product_name, product_price,
                             product_condition, product_image_path, product_seller_id)
VALUES (3, 11, 1, 2, 'PENDING_SHIPMENT', 'Canon EOS 5D Mark IV', 1000, 'LIKE_NEW', './uploads/flyway-order-item-images/canon_1.png', 2);

INSERT IGNORE INTO customer_order(id, placed_at, payment_method, billing_address,
                                 delivery_address, buyer_id, status, stripe_checkout_id)
VALUES (3, UTC_TIMESTAMP(), 'card', "Theodore Meras, Ermou Street, Attica, Athens, 10563, Greece, +302101234567",
        "Theodore Meras, Ermou Street, Attica, Athens, 10563, Greece, +302101234567", 1, "PAID", NULL);
INSERT IGNORE INTO order_item(id, product_id, product_quantity, order_id, status, product_name, product_price,
                             product_condition, product_image_path, product_seller_id)
VALUES (4, 12, 1, 3, 'DELIVERED', 'Nintendo Switch OLED', 35, 'NEW', './uploads/flyway-order-item-images/switch_1.png', 2),
       (5, 13, 1, 3, 'DELIVERED', 'Harry Potter Book Set', 150, 'LIKE_NEW', './uploads/flyway-order-item-images/harry_1.png', 3);

INSERT IGNORE INTO customer_order(id, placed_at, payment_method, billing_address,
                                 delivery_address, buyer_id, status, stripe_checkout_id)
VALUES (4, UTC_TIMESTAMP(), 'card', "Edmund Smith, Mappin Street, South Yorkshire, Sheffield, S1 4DT, United Kingdom, +441234567890",
        "Edmund Smith, Mappin Street, South Yorkshire, Sheffield, S1 4DT, United Kingdom, +441234567890", 2, "PAID", NULL);
INSERT IGNORE INTO order_item(id, product_id, product_quantity, order_id, status, product_name, product_price,
                             product_condition, product_image_path, product_seller_id)
VALUES (6, 15, 1, 4, 'SHIPPED', 'Assassin\'s Creed Valhalla PS4', 60, 'NEW', './uploads/flyway-order-item-images/ac_1.jpeg', 3),
       (7, 16 ,2, 4, 'DELIVERED', 'Xbox Series X 1TB', 450, 'DAMAGED', './uploads/flyway-order-item-images/xbox_1.jpeg', 3);

INSERT IGNORE INTO customer_order(id, placed_at, payment_method, billing_address,
                                 delivery_address, buyer_id, status, stripe_checkout_id)
VALUES (5, UTC_TIMESTAMP(), 'card', "Mary Cole, Oxford Road, Greater Manchester, Manchester, M13 9PL, United Kingdom, +441612345678",
        "Mary Cole, Oxford Road, Greater Manchester, Manchester, M13 9PL, United Kingdom, +441612345678", 3, "PAID", NULL);
INSERT IGNORE INTO order_item(id, product_id, product_quantity, order_id, status, product_name, product_price,
                             product_condition, product_image_path, product_seller_id)
VALUES (8, 4, 2, 5, 'SHIPPED', 'Sony WH-1000XM4', 350, 'LIKE_NEW', './uploads/flyway-order-item-images/xm4_1.png', 1),
       (9, 3, 1, 5, 'PENDING_SHIPMENT', 'Samsung Galaxy S22', 750, 'NEW', './uploads/flyway-order-item-images/s22_1.png', 1);

INSERT IGNORE INTO customer_order(id, placed_at, payment_method, billing_address,
                                 delivery_address, buyer_id, status, stripe_checkout_id)
VALUES (6, UTC_TIMESTAMP(), 'card', "John Doe, 221B Baker Street, Greater London, London, NW1 6XE, United Kingdom, +447911123456",
        "John Doe, 221B Baker Street, Greater London, London, NW1 6XE, United Kingdom, +447911123456", 4, "PAID", NULL);
INSERT IGNORE INTO order_item(id, product_id, product_quantity, order_id, status, product_name, product_price,
                             product_condition, product_image_path, product_seller_id)
VALUES (10, 24, 2, 6, 'DELIVERED', 'HP Officejet Pro 9730e AiO A3 Colour All In One Inkjet Printer', 500, 'LIKE_NEW', './uploads/flyway-order-item-images/officejet_1.png', 3),
       (11, 11, 1, 6, 'SHIPPED', 'Canon EOS 5D Mark IV', 1000, 'LIKE_NEW', './uploads/flyway-order-item-images/canon_1.png', 2);

INSERT IGNORE INTO customer_order(id, placed_at, payment_method, billing_address,
                                 delivery_address, buyer_id, status, stripe_checkout_id)
VALUES (7, UTC_TIMESTAMP(), 'card', "John Doe, 221B Baker Street, Greater London, London, NW1 6XE, United Kingdom, +447911123456",
        "John Doe, 221B Baker Street, Greater London, London, NW1 6XE, United Kingdom, +447911123456", 4, "PAID", NULL);
INSERT IGNORE INTO order_item(id, product_id, product_quantity, order_id, status, product_name, product_price,
                             product_condition, product_image_path, product_seller_id)
VALUES (12, 25, 4, 7, 'PENDING_SHIPMENT', 'Brown Straw Hat', 15, 'NEW', './uploads/flyway-order-item-images/hat_1.png', 3),
       (13, 26, 2, 7, 'SHIPPED', 'Tamaris Leather Black Heels with Strap', 70, 'NEW', './uploads/flyway-order-item-images/heels_1.png', 3);
