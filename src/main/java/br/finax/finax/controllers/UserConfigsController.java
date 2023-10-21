package br.finax.finax.controllers;

import br.finax.finax.models.UserConfigs;
import br.finax.finax.repository.UserConfigsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/user-configs")
public class UserConfigsController {

    @Autowired
    private UserConfigsRepository userConfigsRepository;

    @GetMapping("/{id}")
    private UserConfigs getById(@PathVariable Long id) {
        return userConfigsRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Configurações do usuário não encontradas")
        );
    }

    @GetMapping
    private List<UserConfigs> getAll() {
        return userConfigsRepository.findAll();
    }

    @GetMapping("/get-by-user/{userId}")
    private UserConfigs getByUserId(@PathVariable Long userId) {
        try {
            UserConfigs config = userConfigsRepository.findByUserId(userId);

            if (config == null) throw new RuntimeException();
            return config;

        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Configurações do usuário não encontrada");
        }
    }

    @PostMapping
    private ResponseEntity<UserConfigs> save(@RequestBody UserConfigs userConfigs) {
        try {
            return ResponseEntity.ok().body(userConfigsRepository.save(userConfigs));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao salvar as configurações do usuário");
        }
    }
}
