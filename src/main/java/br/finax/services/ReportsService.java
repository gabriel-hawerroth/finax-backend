package br.finax.services;

import br.finax.dto.FirstAndLastDate;
import br.finax.dto.reports.BalanceEvolutionItem;
import br.finax.dto.reports.CategoryRec;
import br.finax.dto.reports.ReleasesByAccount;
import br.finax.dto.reports.ReleasesByCategory;
import br.finax.enums.release.ReleaseType;
import br.finax.enums.reports.BalanceEvolutionGrouper;
import br.finax.enums.reports.ReportReleasesByInterval;
import br.finax.models.Account;
import br.finax.models.Category;
import br.finax.models.CreditCard;
import br.finax.models.Release;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.finax.utils.UtilsService.getAuthUser;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final ReleaseService releaseService;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final CreditCardService creditCardService;

    private static void validateGetReleasesByParameters(
            ReportReleasesByInterval interval,
            ReleaseType releaseType,
            LocalDate initialDate,
            LocalDate finalDate
    ) {
        if (releaseType == ReleaseType.T) {
            throw new IllegalArgumentException("Release type T is not supported for this report.");
        }

        if (
                !List.of(ReportReleasesByInterval.LAST_30_DAYS, ReportReleasesByInterval.LAST_12_MONTHS).contains(interval)
                        && (initialDate == null || finalDate == null)
        ) {
            throw new IllegalArgumentException("Initial and final date must be provided for monthly and custom reports.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReleasesByCategory> getReleasesByCategory(
            @NonNull ReportReleasesByInterval interval,
            @NonNull ReleaseType releaseType,
            LocalDate initialDate,
            LocalDate finalDate
    ) {
        validateGetReleasesByParameters(interval, releaseType, initialDate, finalDate);
        final FirstAndLastDate firstAndLastDate = getFirstAndLastDate(interval, initialDate, finalDate);
        final List<Release> releases = releaseService.findReleasesForReleasesByCategoryReport(
                getAuthUser().getId(),
                firstAndLastDate.firstDay(),
                firstAndLastDate.lastDay(),
                releaseType
        );
        return groupAndMapReleasesByCategory(releases);
    }

    @Transactional(readOnly = true)
    public List<ReleasesByAccount> getReleasesByAccount(
            @NonNull ReportReleasesByInterval interval,
            @NonNull ReleaseType releaseType,
            LocalDate initialDate,
            LocalDate finalDate
    ) {
        validateGetReleasesByParameters(interval, releaseType, initialDate, finalDate);
        final FirstAndLastDate firstAndLastDate = getFirstAndLastDate(interval, initialDate, finalDate);
        final List<Release> releases = releaseService.findReleasesForReleasesByAccountReport(
                getAuthUser().getId(),
                firstAndLastDate.firstDay(),
                firstAndLastDate.lastDay(),
                releaseType
        );
        return groupAndMapReleasesByAccount(releases);
    }

    @Transactional(readOnly = true)
    public List<BalanceEvolutionItem> getBalanceEvolution(
            @NonNull ReportReleasesByInterval interval,
            LocalDate initialDate,
            LocalDate finalDate,
            Long accountId,
            @NonNull BalanceEvolutionGrouper grouper
    ) {
        validateBalanceEvolutionParameters(interval, initialDate, finalDate);
        final FirstAndLastDate firstAndLastDate = getFirstAndLastDate(interval, initialDate, finalDate);
        final long userId = getAuthUser().getId();
        
        // Buscar o saldo atual da conta ou de todas as contas
        BigDecimal currentBalance = getCurrentBalance(userId, accountId);
        
        // Buscar todos os lançamentos no período
        final List<Release> releases = releaseService.getReleasesForBalanceEvolution(
                userId,
                firstAndLastDate.firstDay(),
                firstAndLastDate.lastDay(),
                accountId
        );
        
        // Calcular o saldo inicial (saldo atual menos os lançamentos do período)
        BigDecimal initialBalance = calculateInitialBalance(currentBalance, releases, accountId);
        
        // Processar lançamentos e gerar evolução do saldo
        return processBalanceEvolution(releases, initialBalance, firstAndLastDate, grouper, accountId);
    }

    private FirstAndLastDate getFirstAndLastDate(ReportReleasesByInterval interval, LocalDate initialDate, LocalDate finalDate) {
        return switch (interval) {
            case MONTHLY, YEARLY, CUSTOM -> new FirstAndLastDate(initialDate, finalDate);
            case LAST_30_DAYS -> new FirstAndLastDate(
                    LocalDate.now().minusDays(30),
                    LocalDate.now()
            );
            case LAST_12_MONTHS -> new FirstAndLastDate(
                    LocalDate.now().minusMonths(12),
                    LocalDate.now()
            );
        };
    }

    private List<ReleasesByCategory> groupAndMapReleasesByCategory(List<Release> releases) {
        if (releases.isEmpty()) return List.of();

        final List<Long> categoryIds = releases.stream()
                .map(Release::getCategoryId)
                .distinct()
                .toList();

        final Map<Long, Category> categoryMap = categoryService.findByIdIn(categoryIds)
                .stream()
                .collect(Collectors.toUnmodifiableMap(Category::getId, category -> category));

        final BigDecimal totalAmount = releases.stream()
                .map(Release::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final Map<Long, BigDecimal> categoryReleaseMap = new HashMap<>();

        releases.forEach(expense -> {
            final BigDecimal categoryExpense = categoryReleaseMap.getOrDefault(expense.getCategoryId(), BigDecimal.ZERO)
                    .add(expense.getAmount());
            categoryReleaseMap.put(expense.getCategoryId(), categoryExpense);
        });

        return categoryReleaseMap.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .map(entry -> {
                    final Category category = categoryMap.get(entry.getKey());
                    final CategoryRec categoryRec = new CategoryRec(category.getName(), category.getColor(), category.getIcon());
                    final BigDecimal percent = entry.getValue().divide(totalAmount, RoundingMode.HALF_EVEN)
                            .multiply(BigDecimal.valueOf(100));
                    return new ReleasesByCategory(categoryRec, percent, entry.getValue());
                })
                .sorted(Comparator.comparing(ReleasesByCategory::value).reversed())
                .toList();
    }

    private List<ReleasesByAccount> groupAndMapReleasesByAccount(List<Release> releases) {
        if (releases.isEmpty()) return List.of();

        final List<Long> accountIds = releases.stream()
                .map(Release::getAccountId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        final List<Long> creditCardsId = releases.stream()
                .map(Release::getCreditCardId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        final Map<Long, Account> accountMap = accountService.findByIdIn(accountIds)
                .stream()
                .collect(Collectors.toUnmodifiableMap(Account::getId, ac -> ac));

        final Map<Long, CreditCard> creditCardMap = creditCardService.findByIdIn(creditCardsId)
                .stream()
                .collect(Collectors.toUnmodifiableMap(CreditCard::getId, cc -> cc));

        final BigDecimal totalAmount = releases.stream()
                .map(Release::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final Map<Long, BigDecimal> accountReleaseMap = new HashMap<>();
        final Map<Long, BigDecimal> creditCardReleaseMap = new HashMap<>();

        releases.forEach(release -> {
            if (release.getAccountId() != null) {
                final BigDecimal accountValue = accountReleaseMap.getOrDefault(release.getAccountId(), BigDecimal.ZERO)
                        .add(release.getAmount());
                accountReleaseMap.put(release.getAccountId(), accountValue);
            }
            if (release.getCreditCardId() != null) {
                final BigDecimal cardValue = creditCardReleaseMap.getOrDefault(release.getCreditCardId(), BigDecimal.ZERO)
                        .add(release.getAmount());
                creditCardReleaseMap.put(release.getCreditCardId(), cardValue);
            }
        });

        List<ReleasesByAccount> accountResults = accountReleaseMap.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .map(entry -> {
                    final Account account = accountMap.get(entry.getKey());
                    final BigDecimal percent = entry.getValue().divide(totalAmount, RoundingMode.HALF_EVEN)
                            .multiply(BigDecimal.valueOf(100));
                    return new ReleasesByAccount(account.getName(), percent, entry.getValue());
                })
                .toList();

        List<ReleasesByAccount> creditCardResults = creditCardReleaseMap.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .map(entry -> {
                    final CreditCard card = creditCardMap.get(entry.getKey());
                    final BigDecimal percent = entry.getValue().divide(totalAmount, RoundingMode.HALF_EVEN)
                            .multiply(BigDecimal.valueOf(100));
                    return new ReleasesByAccount(card.getName(), percent, entry.getValue());
                })
                .toList();

        return Stream.concat(accountResults.stream(), creditCardResults.stream())
                .sorted(Comparator.comparing(ReleasesByAccount::accountName))
                .toList();
    }

    private void validateBalanceEvolutionParameters(
            ReportReleasesByInterval interval,
            LocalDate initialDate,
            LocalDate finalDate
    ) {
        if (
                !List.of(ReportReleasesByInterval.LAST_30_DAYS, ReportReleasesByInterval.LAST_12_MONTHS).contains(interval)
                        && (initialDate == null || finalDate == null)
        ) {
            throw new IllegalArgumentException("Initial and final date must be provided for monthly, yearly and custom reports.");
        }
    }

    private BigDecimal getCurrentBalance(long userId, Long accountId) {
        if (accountId != null) {
            // Saldo de uma conta específica
            Account account = accountService.findById(accountId);
            if (account.getUserId() != userId) {
                throw new IllegalArgumentException("Account does not belong to the user");
            }
            return account.getBalance();
        } else {
            // Saldo de todas as contas
            return accountService.getByUser()
                    .stream()
                    .filter(Account::isAddToCashFlow)
                    .map(Account::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    private BigDecimal calculateInitialBalance(BigDecimal currentBalance, List<Release> releases, Long accountId) {
        BigDecimal totalReleaseImpact = BigDecimal.ZERO;

        for (Release release : releases) {
            BigDecimal impact = calculateReleaseImpact(release, accountId);
            totalReleaseImpact = totalReleaseImpact.add(impact);
        }

        return currentBalance.subtract(totalReleaseImpact);
    }

    private BigDecimal calculateReleaseImpact(Release release, Long accountId) {
        if (accountId != null) {
            // Para conta específica
            if (release.getType() == ReleaseType.T) {
                // Transferência
                if (Objects.equals(release.getTargetAccountId(), accountId)) {
                    return release.getAmount(); // Entrada na conta
                } else if (Objects.equals(release.getAccountId(), accountId)) {
                    return release.getAmount().negate(); // Saída da conta
                }
                return BigDecimal.ZERO; // Não afeta esta conta
            } else if (Objects.equals(release.getAccountId(), accountId)) {
                // Receita ou despesa na conta específica
                return release.getType() == ReleaseType.R 
                    ? release.getAmount() 
                    : release.getAmount().negate();
            }
            return BigDecimal.ZERO;
        } else {
            // Para todas as contas - transferências são ignoradas
            if (release.getType() == ReleaseType.T) {
                return BigDecimal.ZERO; // Transferências não afetam o saldo total
            }
            // Só considera lançamentos em contas que participam do fluxo de caixa
            if (release.getAccountId() != null) {
                return release.getType() == ReleaseType.R 
                    ? release.getAmount() 
                    : release.getAmount().negate();
            }
            // Lançamentos de cartão de crédito são sempre despesas no contexto geral
            return release.getAmount().negate();
        }
    }

    private List<BalanceEvolutionItem> processBalanceEvolution(
            List<Release> releases,
            BigDecimal initialBalance,
            FirstAndLastDate dateRange,
            BalanceEvolutionGrouper grouper,
            Long accountId
    ) {
        if (grouper == BalanceEvolutionGrouper.BY_RELEASE) {
            return processBalanceEvolutionByRelease(releases, initialBalance, accountId);
        } else {
            return processBalanceEvolutionByPeriod(releases, initialBalance, dateRange, grouper, accountId);
        }
    }

    private List<BalanceEvolutionItem> processBalanceEvolutionByRelease(
            List<Release> releases,
            BigDecimal initialBalance,
            Long accountId
    ) {
        List<BalanceEvolutionItem> result = new ArrayList<>();
        BigDecimal runningBalance = initialBalance;

        for (Release release : releases) {
            BigDecimal impact = calculateReleaseImpact(release, accountId);
            runningBalance = runningBalance.add(impact);
            result.add(new BalanceEvolutionItem(release.getDate(), runningBalance));
        }

        return result;
    }

    private List<BalanceEvolutionItem> processBalanceEvolutionByPeriod(
            List<Release> releases,
            BigDecimal initialBalance,
            FirstAndLastDate dateRange,
            BalanceEvolutionGrouper grouper,
            Long accountId
    ) {
        Map<LocalDate, BigDecimal> balanceByPeriod = new HashMap<>();
        BigDecimal runningBalance = initialBalance;

        // Processar todos os lançamentos
        for (Release release : releases) {
            BigDecimal impact = calculateReleaseImpact(release, accountId);
            runningBalance = runningBalance.add(impact);
            
            LocalDate periodKey = getPeriodKey(release.getDate(), grouper);
            balanceByPeriod.put(periodKey, runningBalance);
        }

        // Gerar lista de períodos e preencher com os saldos
        List<LocalDate> periods = generatePeriods(dateRange.firstDay(), dateRange.lastDay(), grouper);
        List<BalanceEvolutionItem> result = new ArrayList<>();
        
        BigDecimal lastBalance = initialBalance;
        for (LocalDate period : periods) {
            BigDecimal balance = balanceByPeriod.getOrDefault(period, lastBalance);
            result.add(new BalanceEvolutionItem(period, balance));
            lastBalance = balance;
        }

        return result;
    }

    private LocalDate getPeriodKey(LocalDate date, BalanceEvolutionGrouper grouper) {
        return switch (grouper) {
            case DAILY -> date;
            case WEEKLY -> date.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
            case MONTHLY -> date.withDayOfMonth(1);
            default -> date;
        };
    }

    private List<LocalDate> generatePeriods(LocalDate startDate, LocalDate endDate, BalanceEvolutionGrouper grouper) {
        List<LocalDate> periods = new ArrayList<>();
        LocalDate current = getPeriodKey(startDate, grouper);
        LocalDate end = getPeriodKey(endDate, grouper);

        while (!current.isAfter(end)) {
            periods.add(current);
            current = switch (grouper) {
                case DAILY -> current.plusDays(1);
                case WEEKLY -> current.plusWeeks(1);
                case MONTHLY -> current.plusMonths(1);
                default -> current.plusDays(1);
            };
        }

        return periods;
    }
}
