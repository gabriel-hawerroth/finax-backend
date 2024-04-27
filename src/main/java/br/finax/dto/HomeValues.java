package br.finax.dto;

import br.finax.models.Account;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record HomeValues(
        @NotNull
        InterfacesSQL.HomeBalances balances,

        @NotNull
        List<Account> accountsList,

        @NotNull
        List<InterfacesSQL.MonthlyReleases> upcomingReleasesExpected
) {
}
