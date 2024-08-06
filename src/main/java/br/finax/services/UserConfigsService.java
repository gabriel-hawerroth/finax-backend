package br.finax.services;

import br.finax.enums.user_configs.UserConfigsReleasesViewMode;
import br.finax.enums.user_configs.UserConfigsTheme;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.UserConfigs;
import br.finax.repository.UserConfigsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.finax.utils.UtilsService.getAuthUser;

@Service
@RequiredArgsConstructor
public class UserConfigsService {

    private final UserConfigsRepository userConfigsRepository;

    @Transactional(readOnly = true)
    public UserConfigs getByUser() {
        return userConfigsRepository.findByUserId(getAuthUser().getId())
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public UserConfigs save(UserConfigs userConfigs) {
        if (userConfigs.getId() == null)
            userConfigs.setUserId(getAuthUser().getId());
        else
            checkPermission(userConfigs);

        return userConfigsRepository.save(userConfigs);
    }

    @Transactional
    public void insertNewUserConfigs(long userId) {
        final var userConfigs = new UserConfigs();

        userConfigs.setUserId(userId);
        userConfigs.setTheme(UserConfigsTheme.light);
        userConfigs.setAddingMaterialGoodsToPatrimony(false);
        userConfigs.setLanguage("pt-BR");
        userConfigs.setCurrency("R$");
        userConfigs.setReleasesViewMode(UserConfigsReleasesViewMode.RELEASES);
        userConfigs.setEmailNotifications(true);

        userConfigsRepository.save(userConfigs);
    }

    private void checkPermission(UserConfigs userConfigs) {
        if (!userConfigs.getUserId().equals(getAuthUser().getId()))
            throw new WithoutPermissionException();
    }
}
