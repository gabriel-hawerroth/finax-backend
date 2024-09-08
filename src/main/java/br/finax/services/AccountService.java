package br.finax.services;

import br.finax.dto.InterfacesSQL.BasicAccount;
import br.finax.dto.InterfacesSQL.HomeAccount;
import br.finax.enums.release.ReleaseType;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.Account;
import br.finax.models.Release;
import br.finax.repository.AccountRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static br.finax.utils.UtilsService.getAuthUser;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class AccountService {

    private final AccountRepository accountRepository;
    private final ReleaseService releaseService;

    @Transactional(readOnly = true)
    public Account findById(long id) {
        final Account account = accountRepository.findById(id).orElseThrow(NotFoundException::new);

        checkPermission(account);

        return account;
    }

    @Transactional(readOnly = true)
    public List<Account> getByUser() {
        return accountRepository.findAllByUserIdOrderByIdAsc(getAuthUser().getId());
    }

    @Transactional(readOnly = true)
    public List<BasicAccount> getBasicList() {
        return accountRepository.getBasicList(getAuthUser().getId());
    }

    @Transactional
    public Account createNew(Account account) {
        account.setId(null);
        account.setUserId(getAuthUser().getId());
        return accountRepository.save(account);
    }

    @Transactional
    public Account edit(Account account) {
        checkPermission(account);

        return accountRepository.save(account);
    }

    @Transactional
    public Account adjustBalance(long accountId, @NonNull BigDecimal newBalance) {
        final Account account = findById(accountId);

        checkPermission(account);

        createNewCashFlowRelease(account, newBalance);

        account.setBalance(newBalance);

        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<HomeAccount> getHomeAccountsList() {
        return accountRepository.getHomeAccountsList(
                getAuthUser().getId()
        );
    }

    private void createNewCashFlowRelease(Account account, BigDecimal newBalance) {
        final Release release = new Release();
        release.setBalanceAdjustment(true);
        release.setUserId(account.getUserId());
        release.setDescription("");
        release.setAccountId(account.getId());
        release.setAmount(
                newBalance.compareTo(account.getBalance()) > 0
                        ? newBalance.subtract(account.getBalance())
                        : account.getBalance().subtract(newBalance)
        );
        release.setType(newBalance.compareTo(account.getBalance()) > 0 ? ReleaseType.R : ReleaseType.E);
        release.setDone(true);
        release.setCategoryId(null);
        release.setDate(LocalDate.now());
        release.setRepeat(null);
        release.setTime(LocalDateTime.now().minusHours(3).format(DateTimeFormatter.ofPattern("HH:mm")));

        releaseService.addRelease(release, 0);
    }

    private void checkPermission(Account account) {
        if (account.getUserId() != getAuthUser().getId())
            throw new WithoutPermissionException();
    }
}
