ALTER TABLE passkey ADD COLUMN user_id UUID;

UPDATE passkey p SET user_id = u.id FROM users u WHERE p.user_handle = u.user_handle;

ALTER TABLE passkey ADD CONSTRAINT fk_passkey_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE passkey ADD CONSTRAINT passkey_key_id_unique UNIQUE (key_id);
