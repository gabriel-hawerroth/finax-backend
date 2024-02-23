package br.finax.services;

import br.finax.models.User;
import br.finax.records.HomeValues;
import br.finax.repository.AccountsRepository;
import br.finax.repository.CashFlowRepository;
import br.finax.utils.UtilsService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final AccountsRepository accountsRepository;
    private final CashFlowRepository cashFlowRepository;
    private final UtilsService utilsService;

    public HomeValues getHomeValues(Date firstDt, Date lastDt) {
        final User user = utilsService.getAuthUser();

        return new HomeValues(
                cashFlowRepository.getHomeBalances(user.getId(), firstDt, lastDt),
                accountsRepository.getHomeAccountsList(user.getId()),
                cashFlowRepository.getUpcomingReleasesExpected(user.getId())
        );
    }
}
