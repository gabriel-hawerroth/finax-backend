package br.finax.services;

import br.finax.dto.InterfacesSQL.AccountBasicList;
import br.finax.exceptions.NotFoundException;
import br.finax.models.Account;
import br.finax.models.CashFlow;
import br.finax.repository.AccountsRepository;
import br.finax.utils.UtilsService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AccountService {

    private final AccountsRepository accountsRepository;

    private final CashFlowService cashFlowService;
    private final UtilsService utilsService;

    @Lazy
    public AccountService(AccountsRepository accountsRepository, CashFlowService cashFlowService, UtilsService utilsService) {
        this.accountsRepository = accountsRepository;
        this.cashFlowService = cashFlowService;
        this.utilsService = utilsService;
    }

    public List<Account> getByUser() {
        return accountsRepository.findAllByUserIdOrderByIdAsc(utilsService.getAuthUser().getId());
    }

    public Account getById(long id) {
        return accountsRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    public List<AccountBasicList> getBasicList() {
        return accountsRepository.getBasicList(utilsService.getAuthUser().getId());
    }

    public Account save(Account account) {
        return accountsRepository.save(account);
    }

    public Account adjustBalance(long accountId, BigDecimal newBalance) {
        Account account = accountsRepository.findById(accountId)
                .orElseThrow(NotFoundException::new);

        account.setBalance(newBalance);

        account = accountsRepository.save(account);

        createNewCashFlowRelease(account, newBalance);

        return account;
    }

    private void createNewCashFlowRelease(Account account, BigDecimal newBalance) {
        final CashFlow release = new CashFlow();
        release.setUserId(account.getUserId());
        release.setDescription("");
        release.setAccountId(account.getId());
        release.setAmount(
                newBalance.compareTo(account.getBalance()) > 0
                        ? newBalance.subtract(account.getBalance())
                        : account.getBalance().subtract(newBalance)
        );
        release.setType(newBalance.compareTo(account.getBalance()) > 0 ? "R" : "E");
        release.setDone(true);
        release.setCategoryId(1L);
        release.setDate(LocalDate.now());
        release.setRepeat("");

        cashFlowService.addRelease(release, 0);
    }

    public List<Account> getHomeAccountsList(long userId) {
        return accountsRepository.getHomeAccountsList(userId);
    }
}
