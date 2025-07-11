package br.finax.services;

import br.finax.dto.FirstAndLastDate;
import br.finax.dto.InterfacesSQL.HomeRevenueExpense;
import br.finax.dto.InterfacesSQL.HomeUpcomingRelease;
import br.finax.dto.home.HomeAccount;
import br.finax.dto.home.HomeCreditCard;
import br.finax.dto.home.SpendByCategoryOutput;
import br.finax.enums.home.SpendByCategoryInterval;
import br.finax.enums.release.ReleaseType;
import br.finax.enums.reports.ReportReleasesByInterval;
import br.finax.models.Account;
import br.finax.models.CreditCard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static br.finax.utils.DateUtils.*;
import static br.finax.utils.UtilsService.getAuthUser;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final ReleaseService releaseService;
    private final AccountService accountService;
    private final CreditCardService creditCardService;
    private final InvoiceService invoiceService;
    private final ReportsService reportsService;

    @Transactional(readOnly = true)
    public HomeRevenueExpense getRevenueExpense() {
        return releaseService.getHomeBalances(
                getAuthUser().getId(),
                getFirstDayOfMonth(),
                getLastDayOfMonth());
    }

    @Transactional(readOnly = true)
    public List<HomeAccount> getAccountsList() {
        final List<Account> accounts = accountService.findAllActiveByLoggedUser().stream()
                .filter(Account::isAddOverallBalance)
                .toList();

        final List<Account> primaryAccounts = accounts.stream().filter(account -> account.getPrimaryAccountId() == null)
                .toList();
        final List<Account> subAccounts = accounts.stream().filter(account -> account.getPrimaryAccountId() != null)
                .toList();

        return primaryAccounts.stream().map(account -> {
            final BigDecimal subAccountsBalance = subAccounts.stream()
                    .filter(subAccount -> subAccount.getPrimaryAccountId().equals(account.getId()))
                    .map(Account::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            final BigDecimal balance = account.isGrouper() ? subAccountsBalance
                    : account.getBalance().add(subAccountsBalance);

            return new HomeAccount(
                    account.getId(),
                    account.getName(),
                    account.getImage(),
                    balance,
                    account.getType());
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<HomeUpcomingRelease> getPayableAndReceivableAccounts() {
        final long userId = getAuthUser().getId();

        return releaseService.getPayableAndReceivableAccounts(
                userId, LocalDate.now().minusMonths(1), LocalDate.now().plusDays(20));
    }

    @Transactional(readOnly = true)
    public SpendByCategoryOutput getSpendsByCategory(SpendByCategoryInterval interval) {
        final ReportReleasesByInterval intervalEnum;
        final FirstAndLastDate firstAndLastDate;

        switch (interval) {
            case CURRENT_MONTH -> {
                String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                intervalEnum = ReportReleasesByInterval.MONTHLY;
                firstAndLastDate = getFirstAndLastDayOfMonth(monthYear);
            }
            case LAST_30_DAYS -> {
                intervalEnum = ReportReleasesByInterval.LAST_30_DAYS;
                firstAndLastDate = new FirstAndLastDate(
                        LocalDate.now().minusDays(30),
                        LocalDate.now());
            }
            default -> throw new IllegalArgumentException("Unsupported interval: " + interval);
        }

        var releases = reportsService.getReleasesByCategory(
            intervalEnum,
            ReleaseType.E,
            firstAndLastDate.firstDay(),
            firstAndLastDate.lastDay()
        );

        return new SpendByCategoryOutput(
                releases,
                firstAndLastDate.firstDay(),
                firstAndLastDate.lastDay());
    }

    @Transactional(readOnly = true)
    public List<HomeCreditCard> getCreditCardsList() {
        final long userId = getAuthUser().getId();

        final List<CreditCard> userCreditCards = creditCardService.getByUser(userId);

        return userCreditCards.stream().map(card -> {
            final var currentInvoiceAmount = invoiceService.getCurrentInvoiceAmount(card);
            final var availableLimit = creditCardService.getCardAvailableLimit(card);

            return new HomeCreditCard(
                    card.getId(),
                    card.getName(),
                    card.getImage(),
                    currentInvoiceAmount,
                    availableLimit);
        }).toList();
    }
}
