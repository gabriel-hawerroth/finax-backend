package br.finax.dto;

import br.finax.models.Category;
import br.finax.models.CreditCard;
import br.finax.dto.InterfacesSQL.AccountBasicList;
import br.finax.dto.InterfacesSQL.CardBasicList;

import java.util.List;

public record InvoiceValues(
    CreditCard creditCard,
    List<AccountBasicList> accountsList,
    List<Category> categoriesList,
    List<CardBasicList> creditCardsList
) {}
