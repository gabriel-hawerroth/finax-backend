package br.finax.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
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
}
