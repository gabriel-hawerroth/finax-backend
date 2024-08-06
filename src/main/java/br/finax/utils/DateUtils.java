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
}
