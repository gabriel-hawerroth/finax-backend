package br.finax.services;

import br.finax.dto.HomeValues;
import br.finax.dto.SpendByCategory;
import br.finax.models.CashFlow;
import br.finax.models.Category;
import br.finax.models.User;
import br.finax.repository.AccountsRepository;
import br.finax.repository.CashFlowRepository;
import br.finax.repository.CategoryRepository;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final AccountsRepository accountsRepository;
    private final CashFlowRepository cashFlowRepository;
    private final CategoryRepository categoryRepository;
    private final UtilsService utilsService;

    public HomeValues getHomeValues(Date firstDt, Date lastDt) {
        final User user = utilsService.getAuthUser();

        return new HomeValues(
                cashFlowRepository.getHomeBalances(user.getId(), firstDt, lastDt),
                accountsRepository.getHomeAccountsList(user.getId()),
                cashFlowRepository.getUpcomingReleasesExpected(user.getId())
        );
    }

    public ResponseEntity<List<SpendByCategory>> getSpendsByCategory(Date firstDt, Date lastDt) {
        try {
            final long userId = utilsService.getAuthUser().getId();

            LocalDate startDt = firstDt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDt = lastDt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            final List<CashFlow> expenses =
                    cashFlowRepository.findByUserIdAndDateBetweenAndTypeAndDone(userId, startDt, endDt, "E", true);

            final Map<Long, Category> categoryMap = new HashMap<>();
            List<Long> categoryIds = expenses.stream().map(CashFlow::getCategoryId).toList();
            List<Category> categories = categoryRepository.findByIdIn(categoryIds);
            categories.forEach(category -> categoryMap.put(category.getId(), category));

            BigDecimal totalExpense = expenses.stream().map(CashFlow::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<Long, BigDecimal> categoryExpenseMap = new HashMap<>();
            for (CashFlow expense : expenses) {
                BigDecimal categoryExpense = categoryExpenseMap.getOrDefault(expense.getCategoryId(), BigDecimal.ZERO);
                categoryExpense = categoryExpense.add(expense.getAmount());
                categoryExpenseMap.put(expense.getCategoryId(), categoryExpense);
            }

            List<SpendByCategory> spendByCategories = new ArrayList<>();
            for (Map.Entry<Long, BigDecimal> entry : categoryExpenseMap.entrySet()) {
                Long categoryId = entry.getKey();
                BigDecimal categoryExpense = entry.getValue();
                Category category = categoryMap.get(categoryId);

                double percent = Double.parseDouble(
                        String.valueOf(
                                categoryExpense.divide(totalExpense, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100))
                        )
                );
                spendByCategories.add(new SpendByCategory(category, percent, categoryExpense));
            }

            spendByCategories.sort(Comparator.comparing(SpendByCategory::value).reversed());

            return ResponseEntity.ok(spendByCategories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
