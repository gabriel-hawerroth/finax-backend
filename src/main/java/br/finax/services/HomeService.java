package br.finax.services;

import br.finax.dto.HomeCreditCard;
import br.finax.dto.InterfacesSQL.HomeAccount;
import br.finax.dto.InterfacesSQL.HomeRevenueExpense;
import br.finax.dto.InterfacesSQL.HomeUpcomingRelease;
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
import java.util.LinkedList;
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
    public List<HomeAccount> getAccountsList() {
        return accountService.getHomeAccountsList();
    }

    @Transactional(readOnly = true)
    public List<HomeUpcomingRelease> getUpcomingReleases() {
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

        categoryExpenseMap.forEach((categoryId, expense) -> {
            final Category category = categoryMap.get(categoryId);

            final BigDecimal percent = expense.divide(totalExpense, 10, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_EVEN);

            spendByCategories.add(new SpendByCategory(category, percent, expense));
        });

        spendByCategories.sort(Comparator.comparing(SpendByCategory::value).reversed());

        return spendByCategories;
    }

    @Transactional(readOnly = true)
    public List<HomeCreditCard> getCreditCardsList() {
        final long userId = getAuthUser().getId();

        final List<CreditCard> userCreditCards = creditCardService.findAllByUserId(userId);

        final List<HomeCreditCard> cardsLists = new LinkedList<>();
        userCreditCards.forEach(card -> {
            final var currentInvoiceAmount = invoiceService.getCurrentInvoiceAmount(card);

            cardsLists.add(new HomeCreditCard(
                    card.getId(),
                    card.getName(),
                    card.getImage(),
                    card.getCardLimit(),
                    currentInvoiceAmount
            ));
        });

        return cardsLists;
    }
}
