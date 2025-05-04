ALTER TABLE address
    DROP column deleted,
    ADD column name VARCHAR(255) NULL,
    ADD column phone_number VARCHAR(255) NULL;