package br.finax.services;

import br.finax.dto.FirstAndLastDate;
import br.finax.dto.home.SpendByCategory;
import br.finax.dto.reports.ReportReleasesByCategoryOutput;
import br.finax.enums.release.ReleaseType;
import br.finax.enums.reports.ReportReleasesByInterval;
import br.finax.models.Category;
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

    private static void validateGetReleasesByCategoryParameters(
            ReportReleasesByInterval interval,
            ReleaseType releaseType,
            String monthYear
    ) {
        if (releaseType == ReleaseType.T) {
            throw new IllegalArgumentException("Release type T is not supported for this report.");
        }

        if (interval == ReportReleasesByInterval.MONTHLY && monthYear == null) {
            throw new IllegalArgumentException("Month and year must be provided for monthly reports.");
        }
    }

    @Transactional(readOnly = true)
    public ReportReleasesByCategoryOutput getReleasesByCategory(
            @NonNull ReportReleasesByInterval interval,
            @NonNull ReleaseType releaseType,
            String monthYear
    ) {
        validateGetReleasesByCategoryParameters(interval, releaseType, monthYear);

        final FirstAndLastDate firstAndLastDate = switch (interval) {
            case MONTHLY -> {
                try {
                    yield getFirstAndLastDayOfMonth(monthYear);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid month and year format. Use MM/yyyy.");
                }
            }
            case LAST_30_DAYS -> new FirstAndLastDate(
                    LocalDate.now().minusDays(30),
                    LocalDate.now()
            );
        };

        final List<Release> releases = releaseService.findReleasesForReleasesByCategoryReport(
                getAuthUser().getId(),
                firstAndLastDate.firstDay(),
                firstAndLastDate.lastDay(),
                releaseType
        );

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

        final List<SpendByCategory> releasesByCategories = categoryReleaseMap.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .map(entry -> {
                    final Category category = categoryMap.get(entry.getKey());
                    final BigDecimal percent = entry.getValue().divide(totalAmount, RoundingMode.HALF_EVEN)
                            .multiply(BigDecimal.valueOf(100));
                    return new SpendByCategory(category, percent, entry.getValue());
                })
                .sorted(Comparator.comparing(SpendByCategory::value).reversed())
                .toList();

        return new ReportReleasesByCategoryOutput(releasesByCategories);
    }
}
