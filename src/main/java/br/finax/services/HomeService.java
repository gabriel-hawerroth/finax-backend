package br.finax.services;

import br.finax.dto.HomeValues;
import br.finax.dto.SpendByCategory;
import br.finax.models.CashFlow;
import br.finax.models.Category;
import br.finax.repository.AccountsRepository;
import br.finax.repository.CashFlowRepository;
import br.finax.repository.CategoryRepository;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static br.finax.utils.DateUtils.dateToLocalDate;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final AccountsRepository accountsRepository;
    private final CashFlowRepository cashFlowRepository;
    private final CategoryRepository categoryRepository;
    private final UtilsService utilsService;

    public HomeValues getHomeValues(Date firstDt, Date lastDt) {
        final long userId = utilsService.getAuthUser().getId();

        return new HomeValues(
                cashFlowRepository.getHomeBalances(userId, firstDt, lastDt),
                accountsRepository.getHomeAccountsList(userId),
                cashFlowRepository.getUpcomingReleasesExpected(userId)
        );
    }

    public List<SpendByCategory> getSpendsByCategory(Date firstDt, Date lastDt) {
        final List<CashFlow> expenses = cashFlowRepository.findByUserIdAndDateBetweenAndTypeAndDone(
                utilsService.getAuthUser().getId(),
                dateToLocalDate(firstDt),
                dateToLocalDate(lastDt),
                "E",
                true
        );

        final Map<Long, Category> categoryMap = new HashMap<>();
        final List<Long> categoryIds = expenses.stream().map(CashFlow::getCategoryId).toList();
        final List<Category> categories = categoryRepository.findByIdIn(categoryIds);
        categories.forEach(category -> categoryMap.put(category.getId(), category));

        final BigDecimal totalExpense = expenses.stream()
                .map(CashFlow::getAmount)
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
}
