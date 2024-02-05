package br.finax.controllers;

import br.finax.models.Account;
import br.finax.models.InterfacesSQL;
import br.finax.models.User;
import br.finax.repository.AccountsRepository;
import br.finax.repository.CashFlowRepository;
import br.finax.utils.UtilsService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private CashFlowRepository cashFlowRepository;
    @Autowired
    private UtilsService utilsService;

    @GetMapping("/get-home-values")
    private HomeValues getHomeAccounts() {
        User user = utilsService.getAuthUser();
        HomeValues response = new HomeValues();
        LocalDate dt = LocalDate.now().withDayOfMonth(15);

        response.setGeneralBalance(accountsRepository.getCurrentBalance(user.getId()).get(0));
        response.setMonthlyFlow(cashFlowRepository.getMonthlyBalance(user.getId(), dt).get(0));
        response.setAccountsList(accountsRepository.getHomeAccountsList(user.getId()));
        response.setUpcomingReleasesExpected(cashFlowRepository.getUpcomingReleasesExpected(user.getId()));

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
