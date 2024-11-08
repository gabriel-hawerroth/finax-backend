package br.finax.dto;

import java.time.LocalDate;

public record FirstAndLastDate(
        LocalDate firstDay,
        LocalDate lastDay
) {
}
