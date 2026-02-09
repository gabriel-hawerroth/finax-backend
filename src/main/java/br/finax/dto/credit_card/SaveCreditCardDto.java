package br.finax.dto.credit_card;

import br.finax.models.CreditCard;

import java.math.BigDecimal;

public record SaveCreditCardDto(
        String name,
        BigDecimal cardLimit,
        int closeDay,
        int expiresDay,
        String image,
        long standardPaymentAccountId
) {
    public CreditCard toEntity() {
        final CreditCard card = new CreditCard();
        card.setName(name);
        card.setCardLimit(cardLimit);
        card.setCloseDay(closeDay);
        card.setExpiresDay(expiresDay);
        card.setImage(image);
        card.setStandardPaymentAccountId(standardPaymentAccountId);

        return card;
    }
}
