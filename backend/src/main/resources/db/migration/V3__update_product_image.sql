ALTER TABLE product_image
    RENAME COLUMN file_path TO image_path;

ALTER TABLE product_image
    ADD COLUMN name VARCHAR(255) NULL,
    ADD COLUMN type VARCHAR(255) NULL,
    DROP COLUMN displayed;