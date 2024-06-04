package br.finax.controllers;

import br.finax.services.LoginService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/login")
public class LoginController {

    private final LoginService loginService;

    @GetMapping("/activate-account/{userId}/{token}")
    public ResponseEntity<Void> activateUser(@PathVariable Long userId, @PathVariable @NotNull String token) {
        loginService.activateUser(userId, token);

        final URI uri = URI.create("https://finax.hawetec.com.br/ativacao-da-conta");

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(uri)
                .build();
    }

    @PostMapping("/send-change-password-email")
    public ResponseEntity<Void> sendChangePasswordMail(@RequestParam @NotNull String email) {
        loginService.sendChangePasswordMail(email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/permit-change-password/{userId}/{token}")
    public ResponseEntity<Void> permitChangePassword(@PathVariable long userId, @PathVariable @NotNull String token) {
        loginService.permitChangePassword(userId, token);

        final URI uri = URI.create("https://finax.hawetec.com.br/recuperacao-da-senha/" + userId);

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(uri)
                .build();
    }
}
