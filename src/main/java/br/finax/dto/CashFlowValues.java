package br.finax.dto;

import br.finax.models.Category;
import br.finax.dto.InterfacesSQL.AccountBasicList;
import br.finax.dto.InterfacesSQL.CardBasicList;

import java.util.List;

public record CashFlowValues(
        List<AccountBasicList> accountsList,
        List<Category> categoriesList,
        List<CardBasicList> creditCardsList
) {}
