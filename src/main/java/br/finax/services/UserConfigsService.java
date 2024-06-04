package br.finax.services;

import br.finax.exceptions.NotFoundException;
import br.finax.models.UserConfigs;
import br.finax.repository.UserConfigsRepository;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserConfigsService {

    private final UserConfigsRepository userConfigsRepository;

    private final UtilsService utils;

    public UserConfigs getByUser() {
        return userConfigsRepository.findByUserId(utils.getAuthUser().getId())
                .orElseThrow(NotFoundException::new);
    }

    public UserConfigs save(UserConfigs userConfigs) {
        return userConfigsRepository.save(userConfigs);
    }
}
