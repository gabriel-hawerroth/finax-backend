package br.finax.dto.reports;

import br.finax.models.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReleasesByCategory(
        @NotNull
        Category category,

        @NotNull
        BigDecimal percent,

        @Min(0)
        @NotNull
        BigDecimal value
) {
}
