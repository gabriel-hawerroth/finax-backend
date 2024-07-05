CREATE SEQUENCE public.access_log_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;
-- DROP SEQUENCE public.bank_account_id_seq;

CREATE SEQUENCE public.bank_account_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;
-- DROP SEQUENCE public.cash_flow_id_seq;

CREATE SEQUENCE public.cash_flow_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;
-- DROP SEQUENCE public.category_id_seq;

CREATE SEQUENCE public.category_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;
-- DROP SEQUENCE public.invoice_payment_id_seq;

CREATE SEQUENCE public.invoice_payment_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;
-- DROP SEQUENCE public.token_id_seq;

CREATE SEQUENCE public.token_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;
-- DROP SEQUENCE public.user_configs_id_seq;

CREATE SEQUENCE public.user_configs_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;
-- DROP SEQUENCE public.users_id_seq;

CREATE SEQUENCE public.users_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 2147483647
    START 1
    CACHE 1
    NO CYCLE;
-- public.users definition

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users
(
    id                   serial4                                       NOT NULL,
    email                varchar(40)                                   NOT NULL,
    "password"           bpchar(60)                                    NOT NULL,
    first_name           varchar(30)                                   NOT NULL,
    last_name            varchar(40)                                   NULL,
    "access"             varchar(10)                                   NOT NULL,
    active               bool       DEFAULT true                       NOT NULL,
    can_change_password  bool       DEFAULT false                      NOT NULL,
    signature            varchar(5) DEFAULT 'month'::character varying NOT NULL,
    signature_expiration date                                          NULL,
    profile_image        text                                          NULL,
    CONSTRAINT users_email_key UNIQUE (email),
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

-- public.access_log definition

-- Drop table

-- DROP TABLE public.access_log;

CREATE TABLE public.access_log
(
    id       serial4                               NOT NULL,
    login_dt timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
    user_id  int4                                  NOT NULL,
    CONSTRAINT access_log_pkey PRIMARY KEY (id),
    CONSTRAINT user_id_fk FOREIGN KEY (user_id) REFERENCES public.users (id)
);
CREATE INDEX access_log_user_id_idx ON public.access_log USING btree (user_id);

-- public.bank_account definition

-- Drop table

-- DROP TABLE public.bank_account;

CREATE TABLE public.bank_account
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
    "type"              varchar(2)                   NULL,
    CONSTRAINT accounts_pkey PRIMARY KEY (id),
    CONSTRAINT bank_accounts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users (id)
);
CREATE INDEX bank_accounts_user_id_idx ON public.bank_account USING btree (user_id);

-- public.category definition

-- Drop table

-- DROP TABLE public.category;

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

-- Drop table

-- DROP TABLE public.credit_card;

CREATE TABLE public.credit_card
(
    id                          int4 DEFAULT nextval('bank_account_id_seq'::regclass) NOT NULL,
    user_id                     int4                                                  NOT NULL,
    "name"                      varchar(40)                                           NOT NULL,
    card_limit                  numeric(15, 2)                                        NOT NULL,
    close_day                   numeric(2)                                            NOT NULL,
    expires_day                 numeric(2)                                            NOT NULL,
    image                       varchar(25)                                           NULL,
    standard_payment_account_id int4                                                  NOT NULL,
    active                      bool DEFAULT true                                     NOT NULL,
    CONSTRAINT credit_card_pkey PRIMARY KEY (id),
    CONSTRAINT credit_card_standard_payment_account_fkey FOREIGN KEY (standard_payment_account_id) REFERENCES public.bank_account (id),
    CONSTRAINT credit_card_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users (id)
);
CREATE INDEX credit_card_standard_payment_account_id_idx ON public.credit_card USING btree (standard_payment_account_id);
CREATE INDEX credit_card_user_id_idx ON public.credit_card USING btree (user_id);

-- public.invoice_payment definition

-- Drop table

-- DROP TABLE public.invoice_payment;

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
    invoice_month_year varchar(7)     NOT NULL,
    CONSTRAINT invoice_payment_pkey PRIMARY KEY (id),
    CONSTRAINT invoice_payment_credit_card_id_fk FOREIGN KEY (credit_card_id) REFERENCES public.credit_card (id),
    CONSTRAINT invoice_payment_payment_account_fkey FOREIGN KEY (payment_account_id) REFERENCES public.bank_account (id)
);
CREATE INDEX invoice_payment_credit_card_id_idx ON public.invoice_payment USING btree (credit_card_id);

-- Table Triggers

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

-- public."token" definition

-- Drop table

-- DROP TABLE public."token";

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

-- Drop table

-- DROP TABLE public.user_configs;

CREATE TABLE public.user_configs
(
    id                                 serial4                                           NOT NULL,
    user_id                            int4                                              NOT NULL,
    theme                              varchar(10) DEFAULT 'light'::character varying    NOT NULL,
    adding_material_goods_to_patrimony bool        DEFAULT false                         NOT NULL,
    "language"                         varchar(5)  DEFAULT 'pt-br'::character varying    NOT NULL,
    currency                           varchar(3)  DEFAULT 'R$'::character varying       NOT NULL,
    releases_view_mode                 varchar(8)  DEFAULT 'releases'::character varying NOT NULL,
    CONSTRAINT user_configs_pkey PRIMARY KEY (id),
    CONSTRAINT user_configs_user_id_key UNIQUE (user_id),
    CONSTRAINT user_configs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users (id)
);

-- public.cash_flow definition

-- Drop table

-- DROP TABLE public.cash_flow;

CREATE TABLE public.cash_flow
(
    id                    serial4                                   NOT NULL,
    account_id            int4                                      NULL,
    amount                numeric(15, 2)                            NOT NULL,
    "type"                bpchar(1)                                 NOT NULL,
    done                  bool        DEFAULT true                  NOT NULL,
    target_account_id     int4                                      NULL,
    category_id           int4                                      NULL,
    "date"                date                                      NOT NULL,
    observation           varchar(100)                              NULL,
    description           varchar(50) DEFAULT ''::character varying NULL,
    "time"                varchar(5)  DEFAULT ''::character varying NULL,
    attachment            text                                      NULL,
    attachment_name       text                                      NULL,
    duplicated_release_id int4                                      NULL,
    user_id               int4                                      NOT NULL,
    repeat                varchar(12) DEFAULT ''::character varying NULL,
    fixed_by              varchar(10) DEFAULT ''::character varying NULL,
    credit_card_id        int4                                      NULL,
    CONSTRAINT cash_flow_pkey PRIMARY KEY (id),
    CONSTRAINT cash_flow_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.bank_account (id),
    CONSTRAINT cash_flow_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.category (id),
    CONSTRAINT cash_flow_credit_card_id_fk FOREIGN KEY (credit_card_id) REFERENCES public.credit_card (id),
    CONSTRAINT cash_flow_target_account_id_fkey FOREIGN KEY (target_account_id) REFERENCES public.bank_account (id),
    CONSTRAINT cash_flow_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users (id)
);
CREATE INDEX cash_flow_account_id_idx ON public.cash_flow USING btree (account_id);
CREATE INDEX cash_flow_category_id_idx ON public.cash_flow USING btree (category_id);
CREATE INDEX cash_flow_credit_card_id_idx ON public.cash_flow USING btree (credit_card_id);
CREATE INDEX cash_flow_date_idx ON public.cash_flow USING btree (date);
CREATE INDEX cash_flow_done_idx ON public.cash_flow USING btree (done);
CREATE INDEX cash_flow_duplicated_release_id_idx ON public.cash_flow USING btree (duplicated_release_id);
CREATE INDEX cash_flow_target_account_id_idx ON public.cash_flow USING btree (target_account_id);
CREATE INDEX cash_flow_user_id_idx ON public.cash_flow USING btree (user_id);

-- Table Triggers

create trigger tr_after_events_cash_flow
    after
        insert
        or
        delete
        or
        update
    on
        public.cash_flow
    for each row
execute function fu_after_events_cash_flow();

-- DROP FUNCTION public.fu_after_events_cash_flow();

CREATE OR REPLACE FUNCTION public.fu_after_events_cash_flow()
    RETURNS trigger
AS
$function$
BEGIN
    IF TG_OP = 'INSERT' AND new.done is true THEN

        IF new.account_id IS NOT NULL THEN
            IF new.type = 'R' THEN
                UPDATE bank_account
                SET balance = balance + new.amount
                WHERE id = new.account_id;
            ELSE
                UPDATE bank_account
                SET balance = balance - new.amount
                WHERE id = new.account_id;

                IF new.type = 'T' THEN
                    UPDATE bank_account
                    SET balance = balance + new.amount
                    WHERE id = new.target_account_id;
                END IF;
            END IF;
        END IF;

    ELSEIF TG_OP = 'UPDATE' THEN
        IF new.done is true and old.done is false THEN
            IF new.account_id IS NOT NULL THEN
                IF new.type = 'R' THEN
                    UPDATE bank_account
                    SET balance = balance + new.amount
                    WHERE id = new.account_id;
                ELSE
                    UPDATE bank_account
                    SET balance = balance - new.amount
                    WHERE id = new.account_id;

                    IF new.type = 'T' THEN
                        UPDATE bank_account
                        SET balance = balance + new.amount
                        WHERE id = new.target_account_id;
                    END IF;
                END IF;
            END IF;
        ELSEIF new.done is false AND old.done is true THEN
            IF old.account_id IS NOT NULL THEN
                IF old.type = 'R' THEN
                    UPDATE bank_account
                    SET balance = balance - old.amount
                    WHERE id = old.account_id;
                ELSE
                    UPDATE bank_account
                    SET balance = balance + old.amount
                    WHERE id = old.account_id;

                    IF new.type = 'T' THEN
                        UPDATE bank_account
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
                        UPDATE bank_account
                        SET balance = balance + (new.amount - old.amount)
                        WHERE id = new.account_id;
                    ELSE
                        UPDATE bank_account
                        SET balance = balance - (old.amount - new.amount)
                        WHERE id = new.account_id;
                    END IF;
                ELSE
                    IF new.amount > old.amount THEN
                        UPDATE bank_account
                        SET balance = balance - (new.amount - old.amount)
                        WHERE id = new.account_id;
                    ELSE
                        UPDATE bank_account
                        SET balance = balance + (old.amount - new.amount)
                        WHERE id = new.account_id;
                    END IF;

                    IF new.type = 'T' THEN
                        IF new.amount > old.amount THEN
                            UPDATE bank_account
                            SET balance = balance + (new.amount - old.amount)
                            WHERE id = new.target_account_id;
                        ELSE
                            UPDATE bank_account
                            SET balance = balance - (old.amount - new.amount)
                            WHERE id = new.target_account_id;
                        END IF;
                    END IF;
                END IF;
            ELSE
                IF new.account_id IS NULL AND old.account_id IS NOT NULL THEN -- atualizado de uma conta para cartão
                    UPDATE bank_account
                    SET balance = balance + old.amount
                    WHERE id = old.account_id;
                ELSEIF new.account_id IS NOT NULL AND old.account_id IS NULL THEN -- atualizado de um cartão para conta
                    UPDATE bank_account
                    SET balance = balance - new.amount
                    WHERE id = new.account_id;
                ELSEIF new.account_id IS NULL AND old.account_id IS NULL THEN -- atualizado de um cartão para outro cartão
                --do nothing
                ELSEIF new.account_id <> old.account_id THEN -- atualizado de uma conta para outra conta
                    IF new.type = 'R' THEN
                        UPDATE bank_account
                        SET balance = balance - old.amount
                        WHERE id = old.account_id;

                        UPDATE bank_account
                        SET balance = balance + new.amount
                        WHERE id = new.account_id;
                    ELSE
                        UPDATE bank_account
                        SET balance = balance + old.amount
                        WHERE id = old.account_id;

                        UPDATE bank_account
                        SET balance = balance - new.amount
                        WHERE id = new.account_id;
                    END IF;
                END IF;

                IF new.target_account_id <> old.target_account_id THEN
                    UPDATE bank_account
                    SET balance = balance - old.amount
                    WHERE id = old.target_account_id;

                    UPDATE bank_account
                    SET balance = balance + new.amount
                    WHERE id = new.target_account_id;
                END IF;
            END IF;
        END IF;

    ELSEIF TG_OP = 'DELETE' AND old.done is true AND old.account_id is not null THEN

        IF old.type = 'R' THEN
            UPDATE bank_account
            SET balance = balance - old.amount
            WHERE id = old.account_id;
        ELSE
            UPDATE bank_account
            SET balance = balance + old.amount
            WHERE id = old.account_id;

            IF old.type = 'T' THEN
                UPDATE bank_account
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

-- DROP FUNCTION public.fu_after_events_invoice_payment();

CREATE OR REPLACE FUNCTION public.fu_after_events_invoice_payment()
    RETURNS trigger
AS
$function$
BEGIN

    if tg_op = 'INSERT' then

        UPDATE bank_account
        SET balance = balance - new.payment_amount
        WHERE id = new.payment_account_id;

    elseif tg_op = 'UPDATE' then

        if new.payment_account_id <> old.payment_account_id then

            UPDATE bank_account
            SET balance = balance + old.payment_amount
            WHERE id = old.payment_account_id;

            UPDATE bank_account
            SET balance = balance - new.payment_amount
            WHERE id = new.payment_account_id;

        elseif new.payment_amount > old.payment_amount then

            UPDATE bank_account
            SET balance = balance - new.payment_amount
            WHERE id = new.payment_account_id;

        elseif new.payment_amount < old.payment_amount then

            UPDATE bank_account
            SET balance = balance + new.payment_amount
            WHERE id = new.payment_account_id;

        end if;

    elseif tg_op = 'DELETE' then

        UPDATE bank_account
        SET balance = balance + old.payment_amount
        WHERE id = old.payment_account_id;

        return old;

    end if;

    return new;

END;
$function$
;
