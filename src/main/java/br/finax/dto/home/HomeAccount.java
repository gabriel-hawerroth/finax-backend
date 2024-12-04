package br.finax.dto.home;

import br.finax.enums.AccountType;

import java.math.BigDecimal;

public record HomeAccount(
        long id,
        String name,
        String image,
        BigDecimal balance,
        AccountType type
) {
}
