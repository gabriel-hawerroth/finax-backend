package br.finax.dto;

import br.finax.dto.InterfacesSQL.AccountBasicList;
import br.finax.dto.InterfacesSQL.CardBasicList;
import br.finax.models.Category;

import java.util.List;

public record CashFlowValues(
        List<AccountBasicList> accountsList,
        List<Category> categoriesList,
        List<CardBasicList> creditCardsList
) {
}
