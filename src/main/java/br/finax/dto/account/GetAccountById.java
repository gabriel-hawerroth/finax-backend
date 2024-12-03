package br.finax.dto.account;

import br.finax.models.Account;

public record GetAccountById(
        Account account,
        Account primaryAccount
) {
}
