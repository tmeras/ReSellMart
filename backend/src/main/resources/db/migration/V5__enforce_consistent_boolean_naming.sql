ALTER TABLE address
    RENAME COLUMN main TO is_main;

ALTER TABLE token
    RENAME COLUMN revoked TO is_revoked;

ALTER TABLE user
    RENAME COLUMN enabled TO is_enabled,
    RENAME COLUMN mfa_enabled TO is_mfa_enabled;
