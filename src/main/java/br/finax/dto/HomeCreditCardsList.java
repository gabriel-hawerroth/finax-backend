package br.finax.dto;

import java.math.BigDecimal;

public record HomeCreditCardsList(
        long cardId,
        String cardName,
        String cardImage,
        BigDecimal cardLimit,
        BigDecimal currentInvoiceAmount
) {
}
