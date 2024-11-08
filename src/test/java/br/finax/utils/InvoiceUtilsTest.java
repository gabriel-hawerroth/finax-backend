package br.finax.utils;

import br.finax.dto.FirstAndLastDate;
import br.finax.exceptions.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static br.finax.utils.InvoiceUtils.getInvoiceCloseAndFirstDay;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class InvoiceUtilsTest {

    @Test
    void testGetInvoiceCloseAndFirstDayWithDay31() {
        FirstAndLastDate result = getInvoiceCloseAndFirstDay("03/2024", 31);

        assertEquals("2024-03-31", result.lastDay().toString());
        assertEquals("2024-03-02", result.firstDay().toString());

        result = getInvoiceCloseAndFirstDay("02/2024", 31);

        assertEquals("2024-03-01", result.lastDay().toString());
        assertEquals("2024-02-01", result.firstDay().toString());

        result = getInvoiceCloseAndFirstDay("01/2024", 31);

        assertEquals("2024-01-31", result.lastDay().toString());
        assertEquals("2024-01-01", result.firstDay().toString());

        result = getInvoiceCloseAndFirstDay("08/2024", 31);

        assertEquals("2024-08-31", result.lastDay().toString());
        assertEquals("2024-08-01", result.firstDay().toString());

        result = getInvoiceCloseAndFirstDay("05/2024", 31);

        assertEquals("2024-05-31", result.lastDay().toString());
        assertEquals("2024-05-02", result.firstDay().toString());
    }

    @Test
    void testGetInvoiceCloseAndFirstDaySuccess() {
        FirstAndLastDate result = getInvoiceCloseAndFirstDay("08/2024", 2);

        assertEquals("2024-08-02", result.lastDay().toString());
        assertEquals("2024-07-03", result.firstDay().toString());

        result = getInvoiceCloseAndFirstDay("02/2024", 25);

        assertEquals("2024-02-25", result.lastDay().toString());
        assertEquals("2024-01-26", result.firstDay().toString());
    }

    @Test
    void testGetInvoiceCloseAndFirstDayInvalidClosingDay() {
        assertAll("Invalid closing day",
                () -> assertThrows(ServiceException.class, () -> getInvoiceCloseAndFirstDay("05/2024", -1)),
                () -> assertThrows(ServiceException.class, () -> getInvoiceCloseAndFirstDay("05/2024", 0)),
                () -> assertThrows(ServiceException.class, () -> getInvoiceCloseAndFirstDay("05/2024", 32))
        );
    }

    @Test
    void testGetInvoiceCloseAndFirstDayInvalidMonthYear() {
        assertAll("Invalid month/year",
                () -> assertThrows(ServiceException.class, () -> getInvoiceCloseAndFirstDay("05/24", 1)),
                () -> assertThrows(ServiceException.class, () -> getInvoiceCloseAndFirstDay("00/2024", 1)),
                () -> assertThrows(ServiceException.class, () -> getInvoiceCloseAndFirstDay("invalid month year", 1))
        );
    }
}
