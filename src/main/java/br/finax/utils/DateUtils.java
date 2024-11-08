package br.finax.utils;

import br.finax.dto.FirstAndLastDate;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;

@UtilityClass
public class DateUtils {

    public static LocalDate getFirstDayOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
    }

    public static LocalDate getLastDayOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Returns the current month and year in MM/yyyy format
     */
    public static String getCurrentMonthYear() {
        final int intCurrentMonth = LocalDate.now().getMonthValue();
        final int intCurrentYear = LocalDate.now().getYear();

        final String stringCurrentMonth = intCurrentMonth < 10 ? "0" + intCurrentMonth : String.valueOf(intCurrentMonth);

        return stringCurrentMonth + "/" + intCurrentYear;
    }

    /**
     * Returns the current month and year in MM/yyyy format
     */
    public static String getNextMonthYear() {
        final String currentMonthYear = getCurrentMonthYear();

        final String month = currentMonthYear.split("/")[0];

        final String nextMonth;
        if (month.equals("12")) {
            nextMonth = "01";
        } else {
            final int intMonth = Integer.parseInt(month) + 1;
            nextMonth = intMonth < 10 ? "0" + intMonth : String.valueOf(intMonth);
        }

        return nextMonth + "/" + currentMonthYear.split("/")[1];
    }

    /**
     * Returns the first and last day of the given month and year
     *
     * @param monthYear the month and year in yyyy-MM format
     * @return an instance of FirstAndLastDate containing the first and last day of the month
     */
    public static FirstAndLastDate getFirstAndLastDayOfMonth(String monthYear) throws DateTimeParseException {
        final YearMonth yearMonth = validateMonthYear(monthYear);

        final LocalDate firstDay = yearMonth.atDay(1);
        final LocalDate lastDay = yearMonth.atEndOfMonth();

        return new FirstAndLastDate(firstDay, lastDay);
    }

    private static YearMonth validateMonthYear(String monthYear) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        return YearMonth.parse(monthYear, formatter);
    }
}
