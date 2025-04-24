CREATE TABLE address
(
    id           INT AUTO_INCREMENT NOT NULL,
    country      VARCHAR(255)       NULL,
    street       VARCHAR(255)       NULL,
    state        VARCHAR(255)       NULL,
    city         VARCHAR(255)       NULL,
    postal_code  VARCHAR(255)       NULL,
    main         BIT(1)             NOT NULL,
    deleted      BIT(1)             NOT NULL,
    address_type VARCHAR(255)       NULL,
    user_id      INT                NOT NULL,
    CONSTRAINT pk_address PRIMARY KEY (id)
);

CREATE TABLE cart_item
(
    id         INT AUTO_INCREMENT NOT NULL,
    product_id INT                NOT NULL,
    quantity   INT                NULL,
    user_id    INT                NOT NULL,
    added_at   DATETIME           NULL,
    CONSTRAINT pk_cartitem PRIMARY KEY (id)
);

CREATE TABLE category
(
    id        INT AUTO_INCREMENT NOT NULL,
    name      VARCHAR(255)       NULL,
    parent_id INT                NULL,
    CONSTRAINT pk_category PRIMARY KEY (id)
);

CREATE TABLE order_item
(
    id               INT AUTO_INCREMENT NOT NULL,
    product_id       INT                NULL,
    product_quantity INT                NULL,
    order_id         INT                NOT NULL,
    CONSTRAINT pk_orderitem PRIMARY KEY (id)
);

CREATE TABLE product
(
    id                 INT AUTO_INCREMENT NOT NULL,
    name               VARCHAR(100)       NULL,
    `description`      VARCHAR(5000)      NULL,
    price              DOUBLE             NULL,
    discounted_price   DOUBLE             NULL,
    product_condition  VARCHAR(255)       NULL,
    available_quantity INT                NULL,
    is_available       BIT(1)             NOT NULL,
    category_id        INT                NOT NULL,
    seller_id          INT                NOT NULL,
    CONSTRAINT pk_product PRIMARY KEY (id)
);

CREATE TABLE product_image
(
    id         INT AUTO_INCREMENT NOT NULL,
    file_path  VARCHAR(500)       NULL,
    displayed  BIT(1)             NOT NULL,
    product_id INT                NOT NULL,
    CONSTRAINT pk_productimage PRIMARY KEY (id)
);

CREATE TABLE customer_order
(
    id                  INT AUTO_INCREMENT NOT NULL,
    placed_at           DATETIME           NULL,
    payment_method      VARCHAR(255)       NULL,
    billing_address_id  INT                NOT NULL,
    delivery_address_id INT                NOT NULL,
    buyer_id            INT                NOT NULL,
    CONSTRAINT pk_order PRIMARY KEY (id)
);

CREATE TABLE `role`
(
    id   INT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255)       NULL,
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE TABLE token
(
    id           INT AUTO_INCREMENT NOT NULL,
    token        VARCHAR(1000)      NULL,
    token_type   VARCHAR(255)       NULL,
    created_at   DATETIME           NULL,
    expires_at   DATETIME           NULL,
    validated_at DATETIME           NULL,
    revoked      BIT(1)             NOT NULL,
    user_id      INT                NOT NULL,
    CONSTRAINT pk_token PRIMARY KEY (id)
);

CREATE TABLE user
(
    id           INT AUTO_INCREMENT NOT NULL,
    name         VARCHAR(255)       NULL,
    email        VARCHAR(255)       NULL,
    password     VARCHAR(255)       NULL,
    home_country VARCHAR(255)       NULL,
    image_path   VARCHAR(1000)      NULL,
    enabled      BIT(1)             NOT NULL,
    mfa_enabled  BIT(1)             NOT NULL,
    secret       VARCHAR(255)       NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE user_roles
(
    user_id  INT NOT NULL,
    roles_id INT NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, roles_id)
);

CREATE TABLE wish_list_item
(
    id         INT AUTO_INCREMENT NOT NULL,
    added_at   DATETIME           NULL,
    product_id INT                NOT NULL,
    user_id    INT                NOT NULL,
    CONSTRAINT pk_wishlistitem PRIMARY KEY (id)
);

ALTER TABLE `role`
    ADD CONSTRAINT uc_role_name UNIQUE (name);

ALTER TABLE user
    ADD CONSTRAINT uc_user_email UNIQUE (email);

ALTER TABLE address
    ADD CONSTRAINT FK_ADDRESS_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE cart_item
    ADD CONSTRAINT FK_CARTITEM_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product (id);

ALTER TABLE cart_item
    ADD CONSTRAINT FK_CARTITEM_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE category
    ADD CONSTRAINT FK_CATEGORY_ON_PARENT FOREIGN KEY (parent_id) REFERENCES category (id);

ALTER TABLE order_item
    ADD CONSTRAINT FK_ORDERITEM_ON_ORDERID FOREIGN KEY (order_id) REFERENCES customer_order (id);

ALTER TABLE order_item
    ADD CONSTRAINT FK_ORDERITEM_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product (id);

ALTER TABLE product_image
    ADD CONSTRAINT FK_PRODUCTIMAGE_ON_PRODUCTID FOREIGN KEY (product_id) REFERENCES product (id);

ALTER TABLE product
    ADD CONSTRAINT FK_PRODUCT_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category (id);

ALTER TABLE product
    ADD CONSTRAINT FK_PRODUCT_ON_SELLER FOREIGN KEY (seller_id) REFERENCES user (id);

ALTER TABLE customer_order
    ADD CONSTRAINT FK_ORDER_ON_BILLINGADDRESS FOREIGN KEY (billing_address_id) REFERENCES address (id);

ALTER TABLE customer_order
    ADD CONSTRAINT FK_ORDER_ON_BUYER FOREIGN KEY (buyer_id) REFERENCES user (id);

ALTER TABLE customer_order
    ADD CONSTRAINT FK_ORDER_ON_DELIVERYADDRESS FOREIGN KEY (delivery_address_id) REFERENCES address (id);

ALTER TABLE token
    ADD CONSTRAINT FK_TOKEN_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE wish_list_item
    ADD CONSTRAINT FK_WISHLISTITEM_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product (id);

ALTER TABLE wish_list_item
    ADD CONSTRAINT FK_WISHLISTITEM_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (roles_id) REFERENCES `role` (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES user (id);