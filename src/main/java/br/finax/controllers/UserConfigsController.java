package br.finax.controllers;

import br.finax.dto.user.UserConfigsDTO;
import br.finax.services.UserConfigsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-configs")
public class UserConfigsController {

    private final UserConfigsService userConfigsService;

    @GetMapping("/get-by-user")
    public ResponseEntity<UserConfigsDTO> getByUser() {
        return ResponseEntity.ok(
                userConfigsService.getByUser()
        );
    }

    @PostMapping("/save")
    public ResponseEntity<UserConfigsDTO> save(@RequestBody @Valid UserConfigsDTO userConfigs) {
        final UserConfigsDTO savedUserConfigs = userConfigsService.save(userConfigs);

        if (userConfigs.id() == null)
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUserConfigs);

        return ResponseEntity.ok(savedUserConfigs);
    }
}
