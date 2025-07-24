package br.finax.dto.reports;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BalanceEvolutionItem(
        LocalDate date,
        BigDecimal amount
) {
}
