package br.finax.controllers;

import br.finax.models.User;
import br.finax.models.UserConfigs;
import br.finax.repository.UserConfigsRepository;
import br.finax.utils.UtilsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/user-configs")
public class UserConfigsController {

    @Autowired
    private UserConfigsRepository userConfigsRepository;
    @Autowired
    private UtilsService utilsService;

    @GetMapping("/get-by-user")
    private UserConfigs getByUserId() {
        try {
            User user = utilsService.getAuthUser();
            UserConfigs config = userConfigsRepository.findByUserId(user.getId());

            if (config == null) throw new RuntimeException();
            return config;
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Configurações do usuário não encontrada");
        }
    }

    @PostMapping("/save")
    private ResponseEntity<UserConfigs> save(@RequestBody UserConfigs userConfigs) {
        try {
            return ResponseEntity.ok().body(userConfigsRepository.save(userConfigs));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao salvar as configurações do usuário");
        }
    }
}
