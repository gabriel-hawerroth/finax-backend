package br.finax.records;

import br.finax.models.Account;
import br.finax.utils.InterfacesSQL;

import java.util.List;

public record HomeValues(
        Double generalBalance,
        InterfacesSQL.MonthlyBalance monthlyFlow,
        List<Account>accountsList,
        List<InterfacesSQL.MonthlyReleases> upcomingReleasesExpected
) {}
