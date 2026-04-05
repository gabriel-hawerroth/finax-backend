CREATE TABLE public.password_recovery_token
(
    id                 bigserial PRIMARY KEY,
    user_id            int4         NOT NULL,
    token_hash         varchar(64)  NOT NULL,
    expires_at         timestamptz  NOT NULL,
    used_at            timestamptz  NULL,
    created_at         timestamptz  NOT NULL DEFAULT current_timestamp,
    created_ip         varchar(64)  NULL,
    request_user_agent varchar(255) NULL,
    CONSTRAINT password_recovery_token_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES public.users (id)
);

CREATE UNIQUE INDEX password_recovery_token_token_hash_uq
    ON public.password_recovery_token (token_hash);

CREATE INDEX password_recovery_token_user_id_idx
    ON public.password_recovery_token (user_id);

CREATE INDEX password_recovery_token_expires_at_idx
    ON public.password_recovery_token (expires_at);

CREATE INDEX password_recovery_token_active_idx
    ON public.password_recovery_token (user_id, used_at, expires_at);
