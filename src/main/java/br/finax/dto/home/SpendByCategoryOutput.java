package br.finax.dto.home;

import java.time.LocalDate;
import java.util.List;

public record SpendByCategoryOutput(
        List<SpendByCategory> spendByCategories,
        LocalDate startDate,
        LocalDate endDate
) {
}
