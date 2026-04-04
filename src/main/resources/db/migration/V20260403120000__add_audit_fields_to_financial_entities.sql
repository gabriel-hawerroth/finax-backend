ALTER TABLE release
    ADD COLUMN created_at TIMESTAMPTZ,
    ADD COLUMN updated_at TIMESTAMPTZ;

ALTER TABLE account
    ADD COLUMN created_at TIMESTAMPTZ,
    ADD COLUMN updated_at TIMESTAMPTZ;

ALTER TABLE credit_card
    ADD COLUMN created_at TIMESTAMPTZ,
    ADD COLUMN updated_at TIMESTAMPTZ;

ALTER TABLE category
    ADD COLUMN created_at TIMESTAMPTZ,
    ADD COLUMN updated_at TIMESTAMPTZ;

UPDATE release
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP);

UPDATE account
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP);

UPDATE credit_card
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP);

UPDATE category
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP);

ALTER TABLE release
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE account
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE credit_card
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE category
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

CREATE OR REPLACE FUNCTION public.fu_before_events_audit_fields()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$function$
BEGIN

    IF NEW.created_at IS NULL THEN
        NEW.created_at := CURRENT_TIMESTAMP;
    END IF;

    NEW.updated_at := CURRENT_TIMESTAMP;

    RETURN NEW;

END;
$function$;

CREATE TRIGGER tr_before_events_release_audit_fields
    BEFORE INSERT OR UPDATE
    ON public.release
    FOR EACH ROW
EXECUTE FUNCTION public.fu_before_events_audit_fields();

CREATE TRIGGER tr_before_events_account_audit_fields
    BEFORE INSERT OR UPDATE
    ON public.account
    FOR EACH ROW
EXECUTE FUNCTION public.fu_before_events_audit_fields();

CREATE TRIGGER tr_before_events_credit_card_audit_fields
    BEFORE INSERT OR UPDATE
    ON public.credit_card
    FOR EACH ROW
EXECUTE FUNCTION public.fu_before_events_audit_fields();

CREATE TRIGGER tr_before_events_category_audit_fields
    BEFORE INSERT OR UPDATE
    ON public.category
    FOR EACH ROW
EXECUTE FUNCTION public.fu_before_events_audit_fields();
