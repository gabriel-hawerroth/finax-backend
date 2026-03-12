package br.finax.dto.cash_flow;

import java.time.LocalDate;

public record FirstAndLastDate(
        LocalDate firstDay,
        LocalDate lastDay
) {
}
