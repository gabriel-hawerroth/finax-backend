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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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
            List<Long> categoryIds = expenses.stream().map(CashFlow::getCategoryId).collect(Collectors.toList());
            List<Category> categories = categoryRepository.findByIdIn(categoryIds);
            categories.forEach(category -> categoryMap.put(category.getId(), category));

            double totalExpense = expenses.stream().mapToDouble(CashFlow::getAmount).sum();

            Map<Long, Double> categoryExpenseMap = new HashMap<>();
            for (CashFlow expense : expenses) {
                double categoryExpense = categoryExpenseMap.getOrDefault(expense.getCategoryId(), 0.0);
                categoryExpense += expense.getAmount();
                categoryExpenseMap.put(expense.getCategoryId(), categoryExpense);
            }

            List<SpendByCategory> spendByCategories = new ArrayList<>();
            for (Map.Entry<Long, Double> entry : categoryExpenseMap.entrySet()) {
                Long categoryId = entry.getKey();
                Double categoryExpense = entry.getValue();
                Category category = categoryMap.get(categoryId);

                double percent = (categoryExpense / totalExpense) * 100;
                spendByCategories.add(new SpendByCategory(category, percent, categoryExpense));
            }

            spendByCategories.sort((a, b) -> Double.compare(b.value(), a.value()));

            return ResponseEntity.ok(spendByCategories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
