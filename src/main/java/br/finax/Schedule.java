package br.finax;

import br.finax.models.Category;

import br.finax.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Schedule {

    private final CategoryRepository categoryRepository;

    @PersistenceContext
    private final EntityManager entityManager;

//    @Scheduled(cron = "0 0 3 * * *") //every day at 3:00 AM
//    public void calculateYields() {
//         logic to analyze all investments and calculate returns based on liquidity and profitability
//    }

    @Scheduled(cron = "0 * * * * *") //every minute
    public void stayActive() {
        // Method for not leaving the machine idle
        List<Category> categories = categoryRepository.findAll();

        for (var j = 0; j < categories.size(); j++) {
            categories.remove(categories.get(j));
        }
    }

    @Scheduled(cron = "0 30 3 28 * *") //28th of every month
    @Transactional
    public void optimizeDatabase() {
        entityManager.createNativeQuery("vacuum full analyze").executeUpdate();
        entityManager.createNativeQuery("reindex database finax_db").executeUpdate();
    }

    @Scheduled(cron = "0 30 21 15 * *")
    @Transactional
    public void optimizeDatabaseTest() {
        entityManager.createNativeQuery("vacuum full analyze").executeUpdate();
        entityManager.createNativeQuery("reindex database finax_db").executeUpdate();
    }
}
