package br.finax.utils;

import br.finax.dto.FirstAndLastDate;
import br.finax.enums.ErrorCategory;
import br.finax.exceptions.ServiceException;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public class InvoiceUtils {

    public static FirstAndLastDate getInvoiceCloseAndFirstDay(String monthYear, int closingDay) {
        if (closingDay < 1 || closingDay > 31)
            throw new ServiceException(ErrorCategory.BAD_REQUEST, "Invalid closing day");

        final InvoiceCloseDay closeDay = getCloseDayInvoice(monthYear, closingDay);
        LocalDate firstDay = closeDay.closeDay().minusMonths(1);

        if (!closeDay.adjusted())
            firstDay = firstDay.plusDays(1);

        if (closingDay > 28) {
            final String lastMonthYear = getLastMonthYear(monthYear);

            final InvoiceCloseDay lastMonthCloseDay = getCloseDayInvoice(lastMonthYear, closingDay);
            if (lastMonthCloseDay.adjusted())
                firstDay = firstDay.plusDays(1);
        }

        return new FirstAndLastDate(firstDay, closeDay.closeDay());
    }

    private static InvoiceCloseDay getCloseDayInvoice(String monthYear, int closingDay) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(monthYear, formatter);
        } catch (DateTimeParseException e) {
            throw new ServiceException(ErrorCategory.BAD_REQUEST, "Invalid date format, expected MM/yyyy");
        }

        final boolean adjusted;

        LocalDate closeDay;
        if (closingDay > yearMonth.lengthOfMonth()) {
            adjusted = true;
            closeDay = yearMonth.atEndOfMonth().plusDays(1);
        } else {
            adjusted = false;
            closeDay = yearMonth.atDay(closingDay);
        }

        return new InvoiceCloseDay(closeDay, adjusted);
    }

    private static String getLastMonthYear(String monthYear) {
        int month = Integer.parseInt(monthYear.split("/")[0]);
        if (month == 1) {
            month = 12;
        } else {
            month--;
        }

        final String monthString = month < 10 ? "0" + month : String.valueOf(month);

        return monthString + "/" + monthYear.split("/")[1];
    }

    public record InvoiceCloseDay(
            LocalDate closeDay,
            boolean adjusted
    ) {
    }
}
