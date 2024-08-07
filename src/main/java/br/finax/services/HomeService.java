package br.finax.services;

import br.finax.dto.InterfacesSQL.HomeAccountsList;
import br.finax.dto.InterfacesSQL.HomeRevenueExpense;
import br.finax.dto.InterfacesSQL.HomeUpcomingReleases;
import br.finax.dto.SpendByCategory;
import br.finax.models.Category;
import br.finax.models.CreditCard;
import br.finax.models.Release;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static br.finax.utils.DateUtils.getFirstDayOfMonth;
import static br.finax.utils.DateUtils.getLastDayOfMonth;
import static br.finax.utils.UtilsService.getAuthUser;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final ReleaseService releaseService;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final CreditCardService creditCardService;
    private final InvoiceService invoiceService;

    @Transactional(readOnly = true)
    public HomeRevenueExpense getRevenueExpense() {
        return releaseService.getHomeBalances(
                getAuthUser().getId(),
                getFirstDayOfMonth(),
                getLastDayOfMonth()
        );
    }

    @Transactional(readOnly = true)
    public List<HomeAccountsList> getAccountsList() {
        return accountService.getHomeAccountsList();
    }

    @Transactional(readOnly = true)
    public List<HomeUpcomingReleases> getUpcomingReleases() {
        final long userId = getAuthUser().getId();

        return releaseService.getUpcomingReleases(userId);
    }

    @Transactional(readOnly = true)
    public List<SpendByCategory> getSpendsByCategory() {
        final List<Release> expenses = releaseService.findReleasesForHomeSpendsCategory(
                getAuthUser().getId(),
                getFirstDayOfMonth(),
                getLastDayOfMonth()
        );

        final Map<Long, Category> categoryMap = new HashMap<>();
        final List<Long> categoryIds = expenses.stream().map(Release::getCategoryId).toList();
        final List<Category> categories = categoryService.findByIdIn(categoryIds);
        categories.forEach(category -> categoryMap.put(category.getId(), category));

        final BigDecimal totalExpense = expenses.stream()
                .map(Release::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final Map<Long, BigDecimal> categoryExpenseMap = new HashMap<>();
        expenses.forEach(expense -> {
            final BigDecimal categoryExpense = categoryExpenseMap.getOrDefault(expense.getCategoryId(), BigDecimal.ZERO)
                    .add(expense.getAmount());
            categoryExpenseMap.put(expense.getCategoryId(), categoryExpense);
        });

        final List<SpendByCategory> spendByCategories = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : categoryExpenseMap.entrySet()) {
            final Long categoryId = entry.getKey();
            final BigDecimal categoryExpense = entry.getValue();
            final Category category = categoryMap.get(categoryId);

            final double percent = Double.parseDouble(
                    String.valueOf(
                            categoryExpense.divide(totalExpense, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100))
                    )
            );
            spendByCategories.add(new SpendByCategory(category, percent, categoryExpense));
        }

        spendByCategories.sort(Comparator.comparing(SpendByCategory::value).reversed());

        return spendByCategories;
    }

    @Transactional(readOnly = true)
    public List<?> getCreditCardsList() {
        final long userId = getAuthUser().getId();

        final List<CreditCard> creditCards = creditCardService.findAllByUserId(userId);

        creditCards.forEach(card -> invoiceService.getCurrentInvoiceValue(card));

        return List.of();
    }
}
