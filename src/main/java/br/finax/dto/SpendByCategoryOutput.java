package br.finax.dto;

import java.time.LocalDate;
import java.util.List;

public record SpendByCategoryOutput(
        List<SpendByCategory> spendByCategories,
        LocalDate startDate,
        LocalDate endDate
) {
}
