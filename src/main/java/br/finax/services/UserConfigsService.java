package br.finax.services;

import br.finax.models.UserConfigs;
import br.finax.repository.UserConfigsRepository;
import br.finax.utils.UtilsService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserConfigsService {

    private final UserConfigsRepository userConfigsRepository;
    private final UtilsService utilsService;

    public UserConfigs getByUser() {
        try {
            return userConfigsRepository.findByUserId(utilsService.getAuthUser().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Configurações do usuário não encontrada");
        }
    }

    public ResponseEntity<UserConfigs> save(UserConfigs userConfigs) {
        try {
            return ResponseEntity.ok().body(userConfigsRepository.save(userConfigs));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao salvar as configurações do usuário");
        }
    }
}
