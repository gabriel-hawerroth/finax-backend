package br.finax.finax;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;

@Component
public class Schedule {

    @Scheduled(cron = "0 0 3 * * *") //every day at 3:00 AM
    public void calculateYields() throws UnknownHostException {
        // logic to analyze all investments and calculate returns based on liquidity and profitability
        System.out.println("Entered scheluded");
    }
}
