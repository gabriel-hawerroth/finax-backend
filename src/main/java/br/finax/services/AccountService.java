package br.finax.services;

import static br.finax.utils.UtilsService.getAuthUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import br.finax.dto.account.SaveAccountDTO;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.finax.dto.InterfacesSQL.BasicAccount;
import br.finax.dto.account.GetAccountById;
import br.finax.enums.ErrorCategory;
import br.finax.enums.release.ReleaseType;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.ServiceException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.Account;
import br.finax.models.Release;
import br.finax.repository.AccountRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class AccountService {

    private final AccountService service;
    private final AccountRepository accountRepository;
    private final ReleaseService releaseService;

    private static void checkPermission(Account account) {
        if (account.getUserId() != getAuthUser().getId())
            throw new WithoutPermissionException();
    }

    @Transactional(readOnly = true)
    public GetAccountById getAccountById(long id) {
        final Account account = service.findById(id);

        final Account primaryAccount = account.getPrimaryAccountId() != null
                ? accountRepository.findById(account.getPrimaryAccountId()).orElseThrow(NotFoundException::new)
                : null;

        return new GetAccountById(account, primaryAccount);
    }

    @Transactional(readOnly = true)
    public Account findById(long id) {
        final Account account = accountRepository.findById(id).orElseThrow(NotFoundException::new);

        checkPermission(account);

        return account;
    }

    @Transactional(readOnly = true)
    public List<Account> findByIdIn(List<Long> id) {
        final List<Account> accounts = accountRepository.findAllById(id);
        accounts.forEach(AccountService::checkPermission);
        return accounts;
    }

    @Transactional(readOnly = true)
    public List<Account> getByUser() {
        return accountRepository.findAllByUserIdOrderByIdAsc(getAuthUser().getId());
    }

    @Transactional(readOnly = true)
    public List<BasicAccount> getBasicList(boolean showSubAccounts) {
        return accountRepository.getBasicList(getAuthUser().getId(), showSubAccounts);
    }

    @Transactional
    public Account createNew(SaveAccountDTO accountDto) {
        final Account account = accountDto.convertToEntity();
        account.setUserId(getAuthUser().getId());
        account.setActive(true);

        if (account.getPrimaryAccountId() != null) {
            final Account primaryAccount = service.findById(account.getPrimaryAccountId());
            checkPermission(primaryAccount);
        }

        return accountRepository.save(account);
    }

    @Transactional
    public Account edit(long accountId, SaveAccountDTO accountDto) {
        final Account account = accountDto.convertToEntity();

        final Account oldAccount = service.findById(accountId);

        account.setId(accountId);
        account.setActive(oldAccount.isActive());
        account.setUserId(oldAccount.getUserId());

        checkPermission(account);

        if (account.getPrimaryAccountId() != null) {
            if (account.getPrimaryAccountId().equals(account.getId()))
                throw new ServiceException(ErrorCategory.BAD_REQUEST, "The account cannot be a sub-account of itself");

            final Account primaryAccount = service.findById(account.getPrimaryAccountId());
            checkPermission(primaryAccount);
        }

        return accountRepository.save(account);
    }

    @Transactional
    public Account adjustBalance(long accountId, @NonNull BigDecimal newBalance) {
        final Account account = service.findById(accountId);

        createNewCashFlowRelease(account, newBalance);

        account.setBalance(newBalance);

        return accountRepository.save(account);
    }

    @Transactional
    public void delete(long accountId) {
        final Account account = service.findById(accountId);

        try {
            if (account.getPrimaryAccountId() == null)
                accountRepository.deleteSubAccounts(accountId);

            accountRepository.deleteById(accountId);
        } catch (DataIntegrityViolationException _) {
            throw new ServiceException(ErrorCategory.BAD_REQUEST, "linked registers, cannot exclude");
        }
    }

    @Transactional
    public void inactivateAccount(long accountId) {
        final var _ = service.findById(accountId);

        accountRepository.inactivateAccount(accountId);
    }

    @Transactional
    public void activateAccount(long accountId, List<Long> subAccountIds) {
        final var _ = service.findById(accountId);

        final List<Long> accountIds = new LinkedList<>();
        accountIds.add(accountId);
        accountIds.addAll(subAccountIds);

        accountRepository.activateAccounts(accountIds);
    }

    @Transactional(readOnly = true)
    public List<Account> findAllActiveByLoggedUser() {
        return accountRepository.findAllActiveByUserId(
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
        release.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        releaseService.addRelease(release, 0);
    }
}
