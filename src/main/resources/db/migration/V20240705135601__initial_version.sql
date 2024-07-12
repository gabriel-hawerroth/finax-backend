CREATE SEQUENCE public.access_log_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.account_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.release_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.category_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.invoice_payment_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.token_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.user_configs_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.users_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;

-- public.users definition

CREATE TYPE user_access AS ENUM ('FREE', 'BASIC', 'PREMIUM', 'ADM');
CREATE TYPE user_signature AS ENUM ('MONTH', 'YEAR');

CREATE TABLE public.users
(
    id                   serial4                                  NOT NULL,
    email                varchar(255)                             NOT NULL,
    "password"           bpchar(60)                               NOT NULL,
    first_name           varchar(30)                              NOT NULL,
    last_name            varchar(40)                              NULL,
    "access"             user_access                              NOT NULL,
    active               bool           DEFAULT true              NOT NULL,
    can_change_password  bool           DEFAULT false             NOT NULL,
    signature            user_signature DEFAULT 'MONTH'           NOT NULL,
    signature_expiration date                                     NULL,
    profile_image        text                                     NULL,
    created_at           timestamptz    DEFAULT current_timestamp NOT NULL,
    CONSTRAINT users_email_key UNIQUE (email),
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

-- public.access_log definition

CREATE TABLE public.access_log
(
    id       serial4                               NOT NULL,
    login_dt timestamptz DEFAULT current_timestamp NOT NULL,
    user_id  int4                                  NOT NULL,
    CONSTRAINT access_log_pkey PRIMARY KEY (id),
    CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES public.users (id)
);
CREATE INDEX access_log_user_id_idx ON public.access_log USING btree (user_id);

-- public.account definition

CREATE TYPE account_type AS ENUM ('CHECKING', 'SAVING', 'SALARY', 'LEGAL', 'BROKERAGE');

CREATE TABLE public.account
(
    id                  serial4                      NOT NULL,
    user_id             int4                         NOT NULL,
    "name"              varchar(40)                  NOT NULL,
    balance             numeric(15, 2) DEFAULT 0     NOT NULL,
    investments         bool           DEFAULT false NOT NULL,
    add_overall_balance bool           DEFAULT true  NOT NULL,
    active              bool           DEFAULT true  NOT NULL,
    archived            bool           DEFAULT false NOT NULL,
    image               varchar(30)                  NULL,
    account_number      varchar(15)                  NULL,
    agency              varchar(5)                   NULL,
    code                numeric(3)                   NULL,
    "type"              account_type                 NULL,
    CONSTRAINT accounts_pkey PRIMARY KEY (id),
    CONSTRAINT accounts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users (id)
);
CREATE INDEX accounts_user_id_idx ON public.account USING btree (user_id);

-- public.category definition

CREATE TABLE public.category
(
    id        serial4            NOT NULL,
    "name"    varchar(40)        NOT NULL,
    color     varchar(10)        NOT NULL,
    icon      varchar(30)        NOT NULL,
    "type"    bpchar(1)          NOT NULL,
    user_id   int4               NOT NULL,
    active    bool DEFAULT true  NOT NULL,
    essential bool DEFAULT false NOT NULL,
    CONSTRAINT category_pkey PRIMARY KEY (id),
    CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES public.users (id)
);
CREATE INDEX category_user_id_idx ON public.category USING btree (user_id);

-- public.credit_card definition

CREATE TABLE public.credit_card
(
    id                          int4 DEFAULT nextval('account_id_seq'::regclass) NOT NULL,
    user_id                     int4                                             NOT NULL,
    "name"                      varchar(40)                                      NOT NULL,
    card_limit                  numeric(15, 2)                                   NOT NULL,
    close_day                   numeric(2)                                       NOT NULL,
    expires_day                 numeric(2)                                       NOT NULL,
    image                       varchar(25)                                      NULL,
    standard_payment_account_id int4                                             NOT NULL,
    active                      bool DEFAULT true                                NOT NULL,
    CONSTRAINT credit_card_pkey PRIMARY KEY (id),
    CONSTRAINT credit_card_standard_payment_account_fkey FOREIGN KEY (standard_payment_account_id) REFERENCES public.account (id),
    CONSTRAINT credit_card_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users (id)
);
CREATE INDEX credit_card_standard_payment_account_id_idx ON public.credit_card USING btree (standard_payment_account_id);
CREATE INDEX credit_card_user_id_idx ON public.credit_card USING btree (user_id);

-- public.invoice_payment definition

CREATE TABLE public.invoice_payment
(
    id                 serial4        NOT NULL,
    payment_account_id int4           NOT NULL,
    payment_amount     numeric(15, 2) NOT NULL,
    payment_date       date           NOT NULL,
    payment_hour       varchar(5)     NULL,
    attachment         bytea          NULL,
    attachment_name    text           NULL,
    credit_card_id     int4           NOT NULL,
    month_year         varchar(7)     NOT NULL,
    CONSTRAINT invoice_payment_pkey PRIMARY KEY (id),
    CONSTRAINT invoice_payment_credit_card_id_fk FOREIGN KEY (credit_card_id) REFERENCES public.credit_card (id),
    CONSTRAINT invoice_payment_payment_account_fkey FOREIGN KEY (payment_account_id) REFERENCES public.account (id)
);
CREATE INDEX invoice_payment_credit_card_id_idx ON public.invoice_payment USING btree (credit_card_id);
CREATE INDEX invoice_payment_payment_account_id_idx ON public.invoice_payment USING btree (payment_account_id);
CREATE INDEX invoice_payment_month_year_idx ON public.invoice_payment USING btree (month_year);

-- public."token" definition

CREATE TABLE public."token"
(
    id      serial4    NOT NULL,
    user_id int4       NOT NULL,
    "token" bpchar(64) NOT NULL,
    CONSTRAINT token_pkey PRIMARY KEY (id),
    CONSTRAINT token_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users (id)
);
CREATE INDEX token_user_id_idx ON public.token USING btree (user_id);

-- public.user_configs definition

CREATE TYPE user_configs_theme AS ENUM ('light', 'dark');
CREATE TYPE user_configs_language AS ENUM ('pt-BR', 'en-US', 'es-CO', 'de-DE');

CREATE TABLE public.user_configs
(
    id                                 serial4                                                  NOT NULL,
    user_id                            int4                                                     NOT NULL,
    theme                              user_configs_theme DEFAULT 'light'                       NOT NULL,
    adding_material_goods_to_patrimony bool               DEFAULT false                         NOT NULL,
    "language"                         varchar(5)         DEFAULT 'pt-BR'                       NOT NULL,
    currency                           varchar(3)         DEFAULT 'R$'::character varying       NOT NULL,
    releases_view_mode                 varchar(8)         DEFAULT 'releases'::character varying NOT NULL,
    CONSTRAINT user_configs_pkey PRIMARY KEY (id),
    CONSTRAINT user_configs_user_id_key UNIQUE (user_id),
    CONSTRAINT user_configs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users (id)
);

-- public.release definition

CREATE TYPE release_type AS ENUM ('E', 'R', 'T');
CREATE TYPE release_repeat AS ENUM ('FIXED', 'INSTALLMENTS');
CREATE TYPE release_fixed_by AS ENUM ('DAILY', 'WEEKLY', 'MONTHLY', 'BIMONTHLY', 'QUARTERLY', 'BIANNUAL', 'ANNUAL');

CREATE TABLE public.release
(
    id                      serial4                   NOT NULL,
    account_id              int4                      NULL,
    amount                  numeric(15, 2)            NOT NULL,
    "type"                  release_type              NOT NULL,
    done                    bool DEFAULT true         NOT NULL,
    target_account_id       int4                      NULL,
    category_id             int4                      NULL,
    "date"                  date DEFAULT current_date NOT NULL,
    observation             varchar(100)              NULL,
    description             varchar(50)               NULL,
    "time"                  varchar(5)                NULL,
    attachment_s3_file_name text                      NULL,
    attachment_name         text                      NULL,
    duplicated_release_id   int4                      NULL,
    user_id                 int4                      NOT NULL,
    repeat                  release_repeat            NULL,
    fixed_by                release_fixed_by          NULL,
    credit_card_id          int4                      NULL,
    is_balance_adjustment   bool DEFAULT false        NOT NULL,
    CONSTRAINT release_pkey PRIMARY KEY (id),
    CONSTRAINT release_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.account (id),
    CONSTRAINT release_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.category (id),
    CONSTRAINT release_credit_card_id_fk FOREIGN KEY (credit_card_id) REFERENCES public.credit_card (id),
    CONSTRAINT release_target_account_id_fkey FOREIGN KEY (target_account_id) REFERENCES public.account (id),
    CONSTRAINT release_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users (id)
);
CREATE INDEX release_account_id_idx ON public.release USING btree (account_id);
CREATE INDEX release_category_id_idx ON public.release USING btree (category_id);
CREATE INDEX release_credit_card_id_idx ON public.release USING btree (credit_card_id);
CREATE INDEX release_date_idx ON public.release USING btree (date);
CREATE INDEX release_done_idx ON public.release USING btree (done);
CREATE INDEX release_duplicated_release_id_idx ON public.release USING btree (duplicated_release_id);
CREATE INDEX release_target_account_id_idx ON public.release USING btree (target_account_id);
CREATE INDEX release_user_id_idx ON public.release USING btree (user_id);

-- Functions

CREATE OR REPLACE FUNCTION public.fu_after_events_release()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$function$
BEGIN
    IF TG_OP = 'INSERT' AND new.done is true THEN

        IF new.account_id IS NOT NULL THEN
            IF new.type = 'R' THEN
                UPDATE account
                SET balance = balance + new.amount
                WHERE id = new.account_id;
            ELSE
                UPDATE account
                SET balance = balance - new.amount
                WHERE id = new.account_id;

                IF new.type = 'T' THEN
                    UPDATE account
                    SET balance = balance + new.amount
                    WHERE id = new.target_account_id;
                END IF;
            END IF;
        END IF;

    ELSEIF TG_OP = 'UPDATE' THEN
        IF new.done is true and old.done is false THEN
            IF new.account_id IS NOT NULL THEN
                IF new.type = 'R' THEN
                    UPDATE account
                    SET balance = balance + new.amount
                    WHERE id = new.account_id;
                ELSE
                    UPDATE account
                    SET balance = balance - new.amount
                    WHERE id = new.account_id;

                    IF new.type = 'T' THEN
                        UPDATE account
                        SET balance = balance + new.amount
                        WHERE id = new.target_account_id;
                    END IF;
                END IF;
            END IF;
        ELSEIF new.done is false AND old.done is true THEN
            IF old.account_id IS NOT NULL THEN
                IF old.type = 'R' THEN
                    UPDATE account
                    SET balance = balance - old.amount
                    WHERE id = old.account_id;
                ELSE
                    UPDATE account
                    SET balance = balance + old.amount
                    WHERE id = old.account_id;

                    IF new.type = 'T' THEN
                        UPDATE account
                        SET balance = balance - old.amount
                        WHERE id = old.target_account_id;
                    END IF;
                END IF;
            END IF;
        ELSEIF ( -- só faz algo se houver alteração no valor, na conta, no cartão ou na conta de destino da transferência
            (COALESCE(new.amount, -1) <> COALESCE(old.amount, -1) OR
             COALESCE(new.account_id, -1) <> COALESCE(old.account_id, -1) OR
             COALESCE(new.credit_card_id, -1) <> COALESCE(old.credit_card_id, -1) OR
             COALESCE(new.target_account_id, -1) <> COALESCE(old.target_account_id, -1))
                AND new.done IS TRUE
            ) THEN
            IF (
                (new.account_id = old.account_id) OR (new.account_id is null AND old.account_id is null)
                    AND
                                                     ((new.target_account_id = old.target_account_id) OR
                                                      (new.target_account_id is null AND old.target_account_id is null))
                    AND
                                                     ((new.credit_card_id = old.credit_card_id) OR
                                                      (new.credit_card_id is null AND old.credit_card_id is null))
                    AND
                                                     (new.account_id IS NOT NULL)
                ) THEN
                -- se tiver atualizado apenas o valor
                IF new.type = 'R' THEN
                    IF new.amount > old.amount THEN
                        UPDATE account
                        SET balance = balance + (new.amount - old.amount)
                        WHERE id = new.account_id;
                    ELSE
                        UPDATE account
                        SET balance = balance - (old.amount - new.amount)
                        WHERE id = new.account_id;
                    END IF;
                ELSE
                    IF new.amount > old.amount THEN
                        UPDATE account
                        SET balance = balance - (new.amount - old.amount)
                        WHERE id = new.account_id;
                    ELSE
                        UPDATE account
                        SET balance = balance + (old.amount - new.amount)
                        WHERE id = new.account_id;
                    END IF;

                    IF new.type = 'T' THEN
                        IF new.amount > old.amount THEN
                            UPDATE account
                            SET balance = balance + (new.amount - old.amount)
                            WHERE id = new.target_account_id;
                        ELSE
                            UPDATE account
                            SET balance = balance - (old.amount - new.amount)
                            WHERE id = new.target_account_id;
                        END IF;
                    END IF;
                END IF;
            ELSE
                IF new.account_id IS NULL AND old.account_id IS NOT NULL THEN -- atualizado de uma conta para cartão
                    UPDATE account
                    SET balance = balance + old.amount
                    WHERE id = old.account_id;
                ELSEIF new.account_id IS NOT NULL AND old.account_id IS NULL THEN -- atualizado de um cartão para conta
                    UPDATE account
                    SET balance = balance - new.amount
                    WHERE id = new.account_id;
                ELSEIF new.account_id IS NULL AND old.account_id IS NULL THEN -- atualizado de um cartão para outro cartão
                --do nothing
                ELSEIF new.account_id <> old.account_id THEN -- atualizado de uma conta para outra conta
                    IF new.type = 'R' THEN
                        UPDATE account
                        SET balance = balance - old.amount
                        WHERE id = old.account_id;

                        UPDATE account
                        SET balance = balance + new.amount
                        WHERE id = new.account_id;
                    ELSE
                        UPDATE account
                        SET balance = balance + old.amount
                        WHERE id = old.account_id;

                        UPDATE account
                        SET balance = balance - new.amount
                        WHERE id = new.account_id;
                    END IF;
                END IF;

                IF new.target_account_id <> old.target_account_id THEN
                    UPDATE account
                    SET balance = balance - old.amount
                    WHERE id = old.target_account_id;

                    UPDATE account
                    SET balance = balance + new.amount
                    WHERE id = new.target_account_id;
                END IF;
            END IF;
        END IF;

    ELSEIF TG_OP = 'DELETE' AND old.done is true AND old.account_id is not null THEN

        IF old.type = 'R' THEN
            UPDATE account
            SET balance = balance - old.amount
            WHERE id = old.account_id;
        ELSE
            UPDATE account
            SET balance = balance + old.amount
            WHERE id = old.account_id;

            IF old.type = 'T' THEN
                UPDATE account
                SET balance = balance - old.amount
                WHERE id = old.target_account_id;
            END IF;
        END IF;

        RETURN OLD;

    END IF;

    RETURN NEW;
END;
$function$
;

CREATE OR REPLACE FUNCTION public.fu_after_events_invoice_payment()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$function$
BEGIN

    if tg_op = 'INSERT' then

        UPDATE account
        SET balance = balance - new.payment_amount
        WHERE id = new.payment_account_id;

    elseif tg_op = 'UPDATE' then

        if new.payment_account_id <> old.payment_account_id then

            UPDATE account
            SET balance = balance + old.payment_amount
            WHERE id = old.payment_account_id;

            UPDATE account
            SET balance = balance - new.payment_amount
            WHERE id = new.payment_account_id;

        elseif new.payment_amount > old.payment_amount then

            UPDATE account
            SET balance = balance - new.payment_amount
            WHERE id = new.payment_account_id;

        elseif new.payment_amount < old.payment_amount then

            UPDATE account
            SET balance = balance + new.payment_amount
            WHERE id = new.payment_account_id;

        end if;

    elseif tg_op = 'DELETE' then

        UPDATE account
        SET balance = balance + old.payment_amount
        WHERE id = old.payment_account_id;

        return old;

    end if;

    return new;

END;
$function$
;

-- Table release Triggers

create trigger tr_after_events_release
    after
        insert
        or
        delete
        or
        update
    on
        public.release
    for each row
execute function fu_after_events_release();

-- Table invoice_payment Triggers

create trigger tr_after_events_invoice_payment
    after
        insert
        or
        delete
        or
        update
    on
        public.invoice_payment
    for each row
execute function fu_after_events_invoice_payment();
