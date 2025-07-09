package br.finax.services;

import br.finax.dto.FirstAndLastDate;
import br.finax.dto.reports.ReportReleasesByCategoryOutput;
import br.finax.dto.reports.ReleasesByCategory;
import br.finax.dto.reports.ReportReleasesByAccountOutput;
import br.finax.dto.reports.ReleasesByAccount;
import br.finax.enums.release.ReleaseType;
import br.finax.enums.reports.ReportReleasesByInterval;
import br.finax.models.Category;
import br.finax.models.Account;
import br.finax.models.Release;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static br.finax.utils.DateUtils.getFirstAndLastDayOfMonth;
import static br.finax.utils.UtilsService.getAuthUser;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final ReleaseService releaseService;
    private final CategoryService categoryService;
    private final AccountService accountService;

    private static void validateGetReleasesByParameters(
            ReportReleasesByInterval interval,
            ReleaseType releaseType,
            LocalDate initialDate,
            LocalDate finalDate
    ) {
        if (releaseType == ReleaseType.T) {
            throw new IllegalArgumentException("Release type T is not supported for this report.");
        }

        if (interval != ReportReleasesByInterval.LAST_30_DAYS && (initialDate == null || finalDate == null)) {
            throw new IllegalArgumentException("Initial and final date must be provided for monthly and custom reports.");
        }
    }

    @Transactional(readOnly = true)
    public ReportReleasesByCategoryOutput getReleasesByCategory(
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
        final List<ReleasesByCategory> releasesByCategories = groupAndMapReleasesByCategory(releases);
        return new ReportReleasesByCategoryOutput(releasesByCategories);
    }

    @Transactional(readOnly = true)
    public ReportReleasesByAccountOutput getReleasesByAccount(
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
        final List<ReleasesByAccount> releasesByAccounts = groupAndMapReleasesByAccount(releases);
        return new ReportReleasesByAccountOutput(releasesByAccounts);
    }

    private FirstAndLastDate getFirstAndLastDate(ReportReleasesByInterval interval, LocalDate initialDate, LocalDate finalDate) {
        return switch (interval) {
            case MONTHLY, CUSTOM -> new FirstAndLastDate(initialDate, finalDate);
            case LAST_30_DAYS -> new FirstAndLastDate(
                LocalDate.now().minusDays(30),
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
                    final BigDecimal percent = entry.getValue().divide(totalAmount, RoundingMode.HALF_EVEN)
                            .multiply(BigDecimal.valueOf(100));
                    return new ReleasesByCategory(category, percent, entry.getValue());
                })
                .sorted(Comparator.comparing(ReleasesByCategory::value).reversed())
                .toList();
    }

    private List<ReleasesByAccount> groupAndMapReleasesByAccount(List<Release> releases) {
        if (releases.isEmpty()) return List.of();
        final List<Long> accountIds = releases.stream()
                .map(Release::getAccountId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        final Map<Long, Account> accountMap = accountService.findByIdIn(accountIds)
                .stream()
                .collect(Collectors.toUnmodifiableMap(Account::getId, ac -> ac));
        final BigDecimal totalAmount = releases.stream()
                .map(Release::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        final Map<Long, BigDecimal> accountReleaseMap = new HashMap<>();
        releases.forEach(release -> {
            if (release.getAccountId() == null) return;
            final BigDecimal accountValue = accountReleaseMap.getOrDefault(release.getAccountId(), BigDecimal.ZERO)
                    .add(release.getAmount());
            accountReleaseMap.put(release.getAccountId(), accountValue);
        });
        return accountReleaseMap.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .map(entry -> {
                    final Account account = accountMap.get(entry.getKey());
                    final BigDecimal percent = entry.getValue().divide(totalAmount, RoundingMode.HALF_EVEN)
                            .multiply(BigDecimal.valueOf(100));
                    return new ReleasesByAccount(account, percent, entry.getValue());
                })
                .sorted(Comparator.comparing(ReleasesByAccount::value).reversed())
                .toList();
    }
}
