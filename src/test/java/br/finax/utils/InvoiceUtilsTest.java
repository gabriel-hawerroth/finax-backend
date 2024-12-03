package br.finax.utils;

import br.finax.dto.FirstAndLastDate;
import br.finax.exceptions.ServiceException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static br.finax.utils.InvoiceUtils.getInvoiceCloseAndFirstDay;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class InvoiceUtilsTest {

    private static Stream<InvoiceDatesTestWithDay31> provideInvoiceDatesTestForGetInvoiceCloseAndFirstDayWithDay31() {
        return Stream.of(
                new InvoiceDatesTestWithDay31("03/2024", "2024-03-31", "2024-03-02"),
                new InvoiceDatesTestWithDay31("02/2024", "2024-03-01", "2024-02-01"),
                new InvoiceDatesTestWithDay31("01/2024", "2024-01-31", "2024-01-01"),
                new InvoiceDatesTestWithDay31("08/2024", "2024-08-31", "2024-08-01"),
                new InvoiceDatesTestWithDay31("05/2024", "2024-05-31", "2024-05-02")
        );
    }

    private static Stream<InvoiceDatesTest> provideInvoiceDatesTestForGetInvoiceCloseAndFirstDay() {
        return Stream.of(
                new InvoiceDatesTest("08/2024", 2, "2024-08-02", "2024-07-03"),
                new InvoiceDatesTest("02/2024", 25, "2024-02-25", "2024-01-26")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvoiceDatesTestForGetInvoiceCloseAndFirstDayWithDay31")
    void testGetInvoiceCloseAndFirstDayWithDay31(InvoiceDatesTestWithDay31 data) {
        FirstAndLastDate result = getInvoiceCloseAndFirstDay(data.monthYear(), 31);

        assertAll("Invalid close or first day",
                () -> assertEquals(data.expectedLastDay(), result.lastDay().toString()),
                () -> assertEquals(data.expectedFirstDay(), result.firstDay().toString())
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvoiceDatesTestForGetInvoiceCloseAndFirstDay")
    void testGetInvoiceCloseAndFirstDaySuccess(InvoiceDatesTest data) {
        FirstAndLastDate dates = getInvoiceCloseAndFirstDay(data.monthYear(), data.closeDay());

        assertAll("Invalid close or first day",
                () -> assertEquals(data.expectedLastDay(), dates.lastDay().toString()),
                () -> assertEquals(data.expectedFirstDay(), dates.firstDay().toString())
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 32})
    void testGetInvoiceCloseAndFirstDayInvalidClosingDay(int day) {
        assertThrows(ServiceException.class, () -> getInvoiceCloseAndFirstDay("05/2024", day));
    }

    @ParameterizedTest
    @ValueSource(strings = {"05/24", "00/2024", "invalid month year"})
    void testGetInvoiceCloseAndFirstDayInvalidMonthYear(String monthYear) {
        assertThrows(ServiceException.class, () -> getInvoiceCloseAndFirstDay(monthYear, 1));
    }

    private record InvoiceDatesTestWithDay31(
            String monthYear,
            String expectedLastDay,
            String expectedFirstDay
    ) {
    }

    private record InvoiceDatesTest(
            String monthYear,
            int closeDay,
            String expectedLastDay,
            String expectedFirstDay
    ) {
    }
}
