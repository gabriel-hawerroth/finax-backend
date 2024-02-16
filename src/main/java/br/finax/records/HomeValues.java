package br.finax.records;

import br.finax.models.Account;
import br.finax.utils.InterfacesSQL;

import java.util.List;

public record HomeValues(
        InterfacesSQL.HomeBalances balances,
        List<Account> accountsList,
        List<InterfacesSQL.MonthlyReleases> upcomingReleasesExpected
) {}
