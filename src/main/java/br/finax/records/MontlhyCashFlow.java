package br.finax.records;

import br.finax.utils.InterfacesSQL;

import java.util.List;

public record MontlhyCashFlow(
        List<InterfacesSQL.MonthlyReleases> releases,
        InterfacesSQL.MonthlyBalance totals
) {}
