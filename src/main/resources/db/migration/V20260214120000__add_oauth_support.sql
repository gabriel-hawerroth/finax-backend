CREATE TYPE auth_provider AS ENUM ('LOCAL', 'GOOGLE');

ALTER TABLE users
    ADD COLUMN provider auth_provider NOT NULL DEFAULT 'LOCAL';

ALTER TABLE users
    ADD COLUMN provider_id varchar(255) NULL;

ALTER TABLE users
    ALTER COLUMN password DROP NOT NULL;

CREATE INDEX users_provider_provider_id_idx ON users (provider, provider_id);
