package br.finax.dto.home;

import java.time.LocalDate;

public record EssentialExpensesOutput(
        Long essentialsAmount,
        Long essentialsPercent,
        Long notEssentialsAmount,
        Long notEssentialsPercent,
        LocalDate startDate,
        LocalDate endDate
) {
}
