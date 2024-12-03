package br.finax.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static br.finax.utils.UtilsService.extractRelevantErrorMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UtilsServiceTest {

    private static final Map<String, String> errorMessages = Map.of(
            "Grouping accounts cannot have a balance", """
                    could not execute statement [ERROR: Grouping accounts cannot have a balance
                      Where: PL/pgSQL function fu_before_events_account() line 7 at RAISE] [update account set account_number=?,active=?,add_overall_balance=?,add_to_cash_flow=?,agency=?,archived=?,balance=?,code=?,grouper=?,image=?,investments=?,name=?,primary_account_id=?,type=? where id=?]
                    """,
            "duplicate key value violates unique constraint \"account_pkey\"", """
                    ERROR: duplicate key value violates unique constraint "account_pkey"
                      Detail: Key (id)=(1) already exists.
                    """
    );

    @Test
    void testExtractRelevantErrorMessage() {
        errorMessages.forEach((expected, argument) -> {
            String actual = extractRelevantErrorMessage(argument);
            assertEquals(expected, actual);
        });
    }
}
