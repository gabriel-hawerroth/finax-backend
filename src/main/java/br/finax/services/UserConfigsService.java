package br.finax.services;

import br.finax.exceptions.NotFoundException;
import br.finax.models.UserConfigs;
import br.finax.repository.UserConfigsRepository;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserConfigsService {

    private final UserConfigsRepository userConfigsRepository;
    private final UtilsService utilsService;

    public UserConfigs getByUser() {
        return userConfigsRepository.findByUserId(utilsService.getAuthUser().getId())
                .orElseThrow(NotFoundException::new);
    }

    public ResponseEntity<UserConfigs> save(UserConfigs userConfigs) {
        return ResponseEntity.ok().body(userConfigsRepository.save(userConfigs));
    }
}
