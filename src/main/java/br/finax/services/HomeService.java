package br.finax.services;

import br.finax.models.User;
import br.finax.records.HomeValues;
import br.finax.repository.AccountsRepository;
import br.finax.repository.CashFlowRepository;
import br.finax.utils.UtilsService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final AccountsRepository accountsRepository;
    private final CashFlowRepository cashFlowRepository;
    private final UtilsService utilsService;

    public HomeValues getHomeValues() {
        User user = utilsService.getAuthUser();

        LocalDate currentDt = LocalDate.now();
        ZoneId systemZone = ZoneId.systemDefault();

        Date firstDate = Date.from(
                currentDt.withDayOfMonth(1).atStartOfDay().atZone(systemZone).toInstant()
        );

        Date lastDate = Date.from(
                currentDt.withDayOfMonth(currentDt.lengthOfMonth()).atTime(23, 59).atZone(ZoneId.systemDefault()).toInstant()
        );

        return new HomeValues(
                cashFlowRepository.getHomeBalances(user.getId(), firstDate, lastDate),
                accountsRepository.getHomeAccountsList(user.getId()),
                cashFlowRepository.getUpcomingReleasesExpected(user.getId())
        );
    }
}
