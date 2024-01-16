package br.finax.controllers;

import br.finax.models.Account;
import br.finax.models.InterfacesSQL;
import br.finax.repository.AccountsRepository;
import br.finax.repository.CashFlowRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private CashFlowRepository cashFlowRepository;

    @GetMapping("/get-home-values/{userId}")
    private HomeValues getHomeAccounts(@PathVariable Long userId) {
        HomeValues response = new HomeValues();

        response.setGeneralBalance(accountsRepository.getCurrentBalance(userId).get(0));
        response.setMonthlyFlow(cashFlowRepository.getMonthlyBalance(userId).get(0));
        response.setAccountsList(accountsRepository.getHomeAccountsList(userId));
        response.setUpcomingReleasesExpected(cashFlowRepository.getUpcomingReleasesExpected(userId));

        return response;
    }

    @Data
    public static class HomeValues {
        Double generalBalance;
        InterfacesSQL.MonthlyBalance monthlyFlow;
        List<Account> accountsList;
        List<InterfacesSQL.MonthlyReleases> upcomingReleasesExpected;
    }
}
