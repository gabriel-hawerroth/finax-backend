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
            SET balance = balance - (new.payment_amount - old.payment_amount)
            WHERE id = new.payment_account_id;

        elseif new.payment_amount < old.payment_amount then

            UPDATE account
            SET balance = balance + (old.payment_amount - new.payment_amount)
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
