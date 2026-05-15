ALTER TABLE passkey
    ADD COLUMN attestation_object TEXT;

ALTER TABLE passkey
    ADD COLUMN client_datajson TEXT;

ALTER TABLE passkey
    ADD COLUMN origin VARCHAR(255);

ALTER TABLE passkey
    ADD COLUMN user_presence BOOLEAN;

ALTER TABLE passkey
    ADD COLUMN user_verification BOOLEAN;

ALTER TABLE passkey
    ADD COLUMN resident_key BOOLEAN;
