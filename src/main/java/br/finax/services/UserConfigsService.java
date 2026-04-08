package br.finax.services;

import br.finax.dto.user.UserConfigsDTO;
import br.finax.enums.user_configs.UserConfigsReleasesViewMode;
import br.finax.enums.user_configs.UserConfigsTheme;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.UserConfigs;
import br.finax.repository.UserConfigsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static br.finax.utils.UtilsService.getAuthUser;

@Service
@RequiredArgsConstructor
public class UserConfigsService {

    private final UserConfigsRepository userConfigsRepository;

    @Transactional
    public UserConfigsDTO getByUser() {
        final long userId = getAuthUser().getId();

        var userConfigs = userConfigsRepository.findByUserId(userId)
                .orElseGet(() -> userConfigsRepository.save(getDefaultUserConfigs(userId)));

        return UserConfigsDTO.fromEntity(userConfigs);
    }

    @Transactional
    public UserConfigsDTO save(UserConfigsDTO userConfigsDto) {
        userConfigsRepository.findById(userConfigsDto.id())
                .ifPresent(this::checkPermission);

        var newUserConfigs = userConfigsDto.toEntity();
        newUserConfigs.setUserId(getAuthUser().getId());

        return UserConfigsDTO.fromEntity(userConfigsRepository.save(newUserConfigs));
    }

    @Transactional
    public void insertUserConfigsIfNotExists(long userId) {
        Optional<UserConfigs> userConfigs = userConfigsRepository.findByUserId(userId);
        if (userConfigs.isPresent()) return;

        userConfigsRepository.save(getDefaultUserConfigs(userId));
    }

    private void checkPermission(UserConfigs userConfigs) {
        if (!userConfigs.getUserId().equals(getAuthUser().getId()))
            throw new WithoutPermissionException();
    }

    private UserConfigs getDefaultUserConfigs(long userId) {
        var configs = new UserConfigs();

        configs.setUserId(userId);
        configs.setTheme(UserConfigsTheme.light);
        configs.setAddingMaterialGoodsToPatrimony(false);
        configs.setLanguage("pt-BR");
        configs.setCurrency("R$");
        configs.setReleasesViewMode(UserConfigsReleasesViewMode.RELEASES);
        configs.setEmailNotifications(true);
        configs.setShowAuditFields(true);

        return configs;
    }
}
