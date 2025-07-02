package br.finax.dto.home;

import java.time.LocalDate;
import java.util.List;

import br.finax.dto.reports.ReleasesByCategory;

public record SpendByCategoryOutput(
        List<ReleasesByCategory> spendByCategories,
        LocalDate startDate,
        LocalDate endDate
) {
}
