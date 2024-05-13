package br.finax.controllers;

import br.finax.models.UserConfigs;
import br.finax.services.UserConfigsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user-configs")
public class UserConfigsController {

    private final UserConfigsService userConfigsService;

    @GetMapping("/get-by-user")
    public UserConfigs getByUser() {
        return userConfigsService.getByUser();
    }

    @PostMapping("/save")
    public ResponseEntity<UserConfigs> save(@RequestBody @Valid UserConfigs userConfigs) {
        return userConfigsService.save(userConfigs);
    }
}
