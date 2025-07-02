package br.finax.dto.reports;

import java.math.BigDecimal;

import br.finax.models.Account;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReleasesByAccount(
        @NotNull
        Account account,

        @NotNull
        BigDecimal percent,

        @Min(0)
        @NotNull
        BigDecimal value
) {
}
