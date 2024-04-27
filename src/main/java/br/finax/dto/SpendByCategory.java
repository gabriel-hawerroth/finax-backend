package br.finax.dto;

import br.finax.models.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SpendByCategory(
        @NotNull
        Category category,

        @NotNull
        double percent,

        @Min(0)
        @NotNull
        BigDecimal value
) {
}
