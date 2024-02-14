package br.finax.services;

import br.finax.models.User;
import br.finax.records.HomeValues;
import br.finax.repository.AccountsRepository;
import br.finax.repository.CashFlowRepository;
import br.finax.utils.UtilsService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final AccountsRepository accountsRepository;
    private final CashFlowRepository cashFlowRepository;
    private final UtilsService utilsService;

    public HomeValues getHomeValues() {
        User user = utilsService.getAuthUser();
        LocalDate dt = LocalDate.now().withDayOfMonth(15);

        return new HomeValues(
                accountsRepository.getCurrentBalance(user.getId()).get(0),
                cashFlowRepository.getMonthlyBalance(user.getId(), dt).get(0),
                accountsRepository.getHomeAccountsList(user.getId()),
                cashFlowRepository.getUpcomingReleasesExpected(user.getId())
        );
    }
}
