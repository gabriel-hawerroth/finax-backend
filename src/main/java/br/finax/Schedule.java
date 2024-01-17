package br.finax;

import br.finax.models.CashFlow;
import br.finax.repository.CashFlowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Schedule {

    @Autowired
    CashFlowRepository cashFlowRepository;

    @Scheduled(cron = "0 0 3 * * *") //every day at 3:00 AM
    public void calculateYields() {
        // logic to analyze all investments and calculate returns based on liquidity and profitability
        System.out.println("Entered calculateYields cron");
    }

    @Scheduled(cron = "0 * * * * *") //every minute
    public void stayActive() {
        // Method for not leaving the machine idle
        System.out.println("Entered Stay Active cron");

        List<CashFlow> cashFlow = cashFlowRepository.findAll();

        for (var j = 0; j < cashFlow.size(); j++) {
            cashFlow.remove(cashFlow.get(j));
        }
    }
}
