CREATE TABLE users
(
    id                      UUID                   NOT NULL,
    username                VARCHAR(255)           NOT NULL,
    user_handle             BYTEA                  NOT NULL,
    assertion               TEXT,
    public_key_json         TEXT,
    registration_complete   BOOLEAN                NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE passkey
(
    id              UUID                   NOT NULL,
    key_id          BYTEA                  NOT NULL,
    public_key      BYTEA                  NOT NULL,
    signature_count INTEGER                NOT NULL,
    transport       VARCHAR(255)           NOT NULL,
    type            VARCHAR(255)           NOT NULL,
    user_handle     BYTEA                  NOT NULL,
    PRIMARY KEY (id)
);