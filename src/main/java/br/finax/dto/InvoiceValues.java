package br.finax.dto;

import br.finax.dto.InterfacesSQL.AccountBasicList;
import br.finax.dto.InterfacesSQL.CardBasicList;
import br.finax.models.Category;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record InvoiceValues(
        @NotNull
        List<AccountBasicList> accountsList,

        @NotNull
        List<Category> categoriesList,

        @NotNull
        List<CardBasicList> creditCardsList
) {
}
