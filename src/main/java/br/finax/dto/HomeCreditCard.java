package br.finax.dto;

import java.math.BigDecimal;

public record HomeCreditCard(
        long cardId,
        String cardName,
        String cardImage,
        BigDecimal cardLimit,
        BigDecimal currentInvoiceAmount
) {
}
