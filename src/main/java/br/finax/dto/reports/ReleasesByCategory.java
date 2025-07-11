package br.finax.dto.reports;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReleasesByCategory(
        @NotNull
        CategoryRec category,

        @NotNull
        BigDecimal percent,

        @Min(0)
        @NotNull
        BigDecimal value
) {
}
