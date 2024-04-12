package br.finax.dto;

import br.finax.models.Account;

import java.util.List;

public record HomeValues(
        InterfacesSQL.HomeBalances balances,
        List<Account> accountsList,
        List<InterfacesSQL.MonthlyReleases> upcomingReleasesExpected
) {
}
