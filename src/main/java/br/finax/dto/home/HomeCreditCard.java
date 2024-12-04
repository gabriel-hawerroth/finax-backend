package br.finax.dto.home;

import java.math.BigDecimal;

public record HomeCreditCard(
        long cardId,
        String cardName,
        String cardImage,
        BigDecimal currentInvoiceAmount,
        BigDecimal availableLimit
) {
}
