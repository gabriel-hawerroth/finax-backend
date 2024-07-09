package br.finax.controllers;

import br.finax.services.LoginService;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Arrays;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final String siteUrl;

    private final LoginService loginService;

    public LoginController(LoginService loginService, Environment environment) {
        this.loginService = loginService;

        if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            siteUrl = "http://localhost:4200";
        } else {
            siteUrl = "https://finax.hawetec.com.br";
        }
    }

    @GetMapping("/activate-account/{userId}/{token}")
    public ResponseEntity<Void> activateUser(@PathVariable Long userId, @PathVariable @NotNull String token) {
        loginService.activateUser(userId, token);

        final URI uri = URI.create(siteUrl + "/ativacao-da-conta");

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

        final URI uri = URI.create(siteUrl + "/recuperacao-da-senha/" + userId);

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(uri)
                .build();
    }
}
