package br.finax.dto.account;

import br.finax.enums.AccountType;
import br.finax.models.Account;

import java.math.BigDecimal;

public record SaveAccountDTO(
        String name,
        BigDecimal balance,
        boolean investments,
        boolean addOverallBalance,
        String image,
        String accountNumber,
        String agency,
        Integer code,
        AccountType type,
        Long primaryAccountId,
        boolean addToCashFlow,
        boolean grouper
) {

    public Account convertToEntity() {
        final Account account = new Account();

        account.setName(name);
        account.setBalance(balance);
        account.setInvestments(investments);
        account.setAddOverallBalance(addOverallBalance);
        account.setImage(image);
        account.setAccountNumber(accountNumber);
        account.setAgency(agency);
        account.setCode(code);
        account.setType(type);
        account.setPrimaryAccountId(primaryAccountId);
        account.setAddToCashFlow(addToCashFlow);
        account.setGrouper(grouper);

        return account;
    }
}
