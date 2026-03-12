package br.finax.dto.credit_card;

import br.finax.dto.InterfacesSQL.BasicAccount;
import br.finax.dto.InterfacesSQL.BasicCard;
import br.finax.models.Category;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record InvoiceValues(
        @NotNull
        List<BasicAccount> accountsList,

        @NotNull
        List<Category> categoriesList,

        @NotNull
        List<BasicCard> creditCardsList
) {
}
