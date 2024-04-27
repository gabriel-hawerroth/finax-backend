package br.finax.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MonthlyCashFlow(
        @NotNull
        List<InterfacesSQL.MonthlyReleases> releases,

        @NotNull
        double expectedBalance
) {
}
