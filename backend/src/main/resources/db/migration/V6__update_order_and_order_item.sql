ALTER TABLE customer_order
    DROP COLUMN delivery_address_id,
    DROP COLUMN billing_address_id,
    DROP FOREIGN KEY FK_ORDER_ON_DELIVERYADDRESS,
    DROP FOREIGN KEY FK_ORDER_ON_BILLINGADDRESS,
    ADD COLUMN delivery_address VARCHAR(255) NULL,
    ADD COLUMN billing_address VARCHAR(255) NULL,
    ADD COLUMN status VARCHAR(255) NULL,
    ADD COLUMN stripe_checkout_id VARCHAR(255) NULL;

ALTER TABLE order_item
    ADD COLUMN status VARCHAR(255) NULL,
    ADD COLUMN product_name VARCHAR(255) NULL,
    ADD COLUMN product_price DECIMAL(7, 2) NULL,
    ADD COLUMN product_condition VARCHAR(255) NULL,
    ADD COLUMN product_image_path VARCHAR(1000) NULL,
    ADD COLUMN product_seller_id INT NOT NULL,
    ADD CONSTRAINT FK_ORDERITEM_ON_USER FOREIGN KEY (product_seller_id) REFERENCES user (id);
