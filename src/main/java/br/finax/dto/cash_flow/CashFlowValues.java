package br.finax.dto.cash_flow;

import br.finax.dto.InterfacesSQL.BasicAccount;
import br.finax.dto.InterfacesSQL.BasicCard;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CashFlowValues(
        @NotNull
        List<BasicAccount> accountsList,

        @NotNull
        List<CashFlowCategory> categories,

        @NotNull
        List<BasicCard> creditCardsList
) {
}
