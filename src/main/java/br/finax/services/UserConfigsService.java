package br.finax.services;

import br.finax.enums.user.UserTheme;
import br.finax.exceptions.NotFoundException;
import br.finax.models.UserConfigs;
import br.finax.repository.UserConfigsRepository;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserConfigsService {

    private final UserConfigsRepository userConfigsRepository;

    private final UtilsService utils;

    @Transactional(readOnly = true)
    public UserConfigs getByUser() {
        return userConfigsRepository.findByUserId(utils.getAuthUser().getId())
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public UserConfigs save(UserConfigs userConfigs) {
        return userConfigsRepository.save(userConfigs);
    }

    @Transactional
    public void insertNewUserConfigs(long userId) {
        final var userConfigs = new UserConfigs();

        userConfigs.setUserId(userId);
        userConfigs.setTheme(UserTheme.light);
        userConfigs.setAddingMaterialGoodsToPatrimony(false);
        userConfigs.setLanguage("pt-BR");
        userConfigs.setCurrency("R$");
        userConfigs.setReleasesViewMode("releases");

        userConfigsRepository.save(userConfigs);
    }
}
