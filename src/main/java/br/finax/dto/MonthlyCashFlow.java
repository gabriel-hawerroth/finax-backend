package br.finax.dto;

import java.util.List;

public record MonthlyCashFlow(
        List<InterfacesSQL.MonthlyReleases> releases,
        double expectedBalance
) {
}
