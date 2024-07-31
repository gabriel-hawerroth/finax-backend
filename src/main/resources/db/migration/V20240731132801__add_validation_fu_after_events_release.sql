CREATE OR REPLACE FUNCTION public.fu_after_events_release()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$function$
BEGIN
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        IF new.account_id IS NULL AND new.credit_card_id IS NULL THEN
            RAISE EXCEPTION 'The release must have an account or credit card ID';
        END IF;
    END IF;

    IF TG_OP = 'INSERT' AND new.done IS TRUE THEN

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

        IF old.done IS FALSE AND new.done IS TRUE THEN
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
        ELSEIF old.done IS TRUE AND new.done IS FALSE THEN
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
                (new.account_id = old.account_id)
                    OR
                (new.account_id IS NULL AND old.account_id IS NULL)
                    AND
                ((new.target_account_id = old.target_account_id) OR
                 (new.target_account_id IS NULL AND old.target_account_id IS NULL))
                    AND
                ((new.credit_card_id = old.credit_card_id) OR
                 (new.credit_card_id IS NULL AND old.credit_card_id IS NULL))
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

    ELSEIF TG_OP = 'DELETE' AND old.done IS TRUE AND old.account_id IS NOT NULL THEN

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