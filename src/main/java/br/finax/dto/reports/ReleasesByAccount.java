package br.finax.dto.reports;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReleasesByAccount(
        @NotNull
        String accountName,

        @NotNull
        BigDecimal percent,

        @Min(0)
        @NotNull
        BigDecimal value
) {
}
