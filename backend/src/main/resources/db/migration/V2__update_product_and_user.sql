ALTER TABLE product RENAME COLUMN is_available TO is_deleted;
ALTER TABLE product RENAME COLUMN discounted_price TO previous_price;

ALTER TABLE user
    ADD COLUMN registered_at DATE NULL;