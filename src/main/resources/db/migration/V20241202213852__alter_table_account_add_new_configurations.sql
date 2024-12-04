ALTER TABLE account
    ADD COLUMN add_to_cash_flow BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE account
    ADD COLUMN grouper BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE account
    DROP COLUMN archived;

-----------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.fu_before_events_account()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$function$
BEGIN

    IF TG_OP <> 'DELETE' THEN

        IF new.grouper IS TRUE AND new.balance > 0 THEN
            RAISE EXCEPTION 'Grouping accounts cannot have a balance';
        END IF;

        IF new.grouper IS TRUE AND new.primary_account_id IS NOT NULL THEN
            RAISE EXCEPTION 'Grouping accounts cannot have a primary account';
        END IF;

        IF new.grouper IS TRUE AND new.type = 'CASH' THEN
            RAISE EXCEPTION 'Grouping accounts cannot have "cash" account type';
        END IF;

        RETURN new;

    END IF;

    RETURN old;

END;
$function$;

-----------------------------------------------------------------------------------------------------------------------

CREATE TRIGGER tr_before_events_account
    BEFORE
        INSERT
        OR
        UPDATE
        OR
        DELETE
    ON
        public.account
    FOR EACH ROW
EXECUTE FUNCTION fu_before_events_account();

-----------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.fu_after_events_account()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$function$
BEGIN

    IF TG_OP <> 'DELETE' THEN

        IF new.grouper IS TRUE THEN
            UPDATE account a
            SET add_to_cash_flow    = new.add_to_cash_flow,
                add_overall_balance = new.add_overall_balance
            WHERE a.primary_account_id = new.id;
        END IF;

        RETURN new;

    END IF;

    RETURN old;

END;
$function$;

-----------------------------------------------------------------------------------------------------------------------

CREATE TRIGGER tr_after_events_account
    AFTER
        INSERT
        OR
        UPDATE
        OR
        DELETE
    ON
        public.account
    FOR EACH ROW
EXECUTE FUNCTION fu_after_events_account();
