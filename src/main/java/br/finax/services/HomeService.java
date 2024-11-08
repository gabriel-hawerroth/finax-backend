package br.finax.services;

import br.finax.dto.HomeCreditCard;
import br.finax.dto.InterfacesSQL.HomeAccount;
import br.finax.dto.InterfacesSQL.HomeRevenueExpense;
import br.finax.dto.InterfacesSQL.HomeUpcomingRelease;
import br.finax.dto.SpendByCategory;
import br.finax.dto.SpendByCategoryOutput;
import br.finax.enums.home.SpendByCategoryInterval;
import br.finax.models.Category;
import br.finax.models.CreditCard;
import br.finax.models.Release;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static br.finax.utils.DateUtils.getFirstDayOfMonth;
import static br.finax.utils.DateUtils.getLastDayOfMonth;
import static br.finax.utils.DateUtils.getNextMonthYear;
import static br.finax.utils.InvoiceUtils.getInvoiceCloseAndFirstDay;
import static br.finax.utils.UtilsService.getAuthUser;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final ReleaseService releaseService;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final CreditCardService creditCardService;
    private final InvoiceService invoiceService;
    private final InvoicePaymentService invoicePaymentService;

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
    public List<HomeUpcomingRelease> getPayableAndReceivableAccounts() {
        final long userId = getAuthUser().getId();

        return releaseService.getPayableAndReceivableAccounts(
                userId, LocalDate.now().minusMonths(1), LocalDate.now().plusDays(20)
        );
    }

    @Transactional(readOnly = true)
    public SpendByCategoryOutput getSpendsByCategory(SpendByCategoryInterval interval) {
        final LocalDate firstDay = switch (interval) {
            case CURRENT_MONTH -> getFirstDayOfMonth();
            case LAST_30_DAYS -> LocalDate.now().minusDays(30);
        };

        final LocalDate lastDay = switch (interval) {
            case CURRENT_MONTH -> getLastDayOfMonth();
            case LAST_30_DAYS -> LocalDate.now();
        };

        final List<Release> expenses = releaseService.findReleasesForHomeSpendsCategory(
                getAuthUser().getId(),
                firstDay,
                lastDay
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

            final BigDecimal percent = expense.divide(totalExpense, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal.valueOf(100));

            spendByCategories.add(new SpendByCategory(category, percent, expense));
        });

        spendByCategories.sort(Comparator.comparing(SpendByCategory::value).reversed());

        return new SpendByCategoryOutput(
                spendByCategories,
                firstDay,
                lastDay
        );
    }

    @Transactional(readOnly = true)
    public List<HomeCreditCard> getCreditCardsList() {
        final long userId = getAuthUser().getId();

        final List<CreditCard> userCreditCards = creditCardService.findAllByUserId(userId);

        final List<HomeCreditCard> cardsLists = new LinkedList<>();

        userCreditCards.forEach(card -> {
            final var closeAndFirstDayInvoice = getInvoiceCloseAndFirstDay(getNextMonthYear(), card.getCloseDay());

            final var currentInvoiceAmount = invoiceService.getCurrentInvoiceAmount(card);
            final var previousInvoiceAmount = invoicePaymentService.getInvoicePreviousBalance(userId, card.getId(), closeAndFirstDayInvoice.firstDay());

            final var nextInvoicesAmount = invoiceService.getCardNextInvoicesAmount(card);

            cardsLists.add(new HomeCreditCard(
                    card.getId(),
                    card.getName(),
                    card.getImage(),
                    card.getCardLimit(),
                    currentInvoiceAmount.add(previousInvoiceAmount),
                    nextInvoicesAmount
            ));
        });

        return cardsLists;
    }
}
