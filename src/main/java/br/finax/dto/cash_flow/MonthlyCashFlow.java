package br.finax.dto.cash_flow;

import br.finax.dto.InterfacesSQL;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MonthlyCashFlow(
        @NotNull
        List<InterfacesSQL.MonthlyRelease> releases,

        @NotNull
        double expectedBalance
) {
}
