package br.finax.controllers;

import br.finax.services.LoginService;
import br.finax.utils.ServiceUrls;
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
    private final ServiceUrls serviceUrls;

    @GetMapping("/activate-account/{userId}/{token}")
    public ResponseEntity<Void> activateUser(@PathVariable Long userId, @PathVariable @NotNull String token) {
        loginService.activateUser(userId, token);

        final URI uri = URI.create(serviceUrls.getSiteUrl() + "/ativacao-da-conta");

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(uri)
                .build();
    }

    @PostMapping("/send-change-password-email")
    public ResponseEntity<Void> sendChangePasswordEmail(@RequestParam @NotNull String email) {
        loginService.sendChangePasswordEmail(email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/permit-change-password/{userId}/{token}")
    public ResponseEntity<Void> permitChangePassword(@PathVariable long userId, @PathVariable @NotNull String token) {
        loginService.permitChangePassword(userId, token);

        final URI uri = URI.create(serviceUrls.getSiteUrl() + "/recuperacao-da-senha/" + userId);

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(uri)
                .build();
    }

    @PostMapping("/send-cancel-account-email/{userId}")
    public ResponseEntity<Void> sendCancelUserAccountEmail(@PathVariable long userId) {
        loginService.sendCancelUserAccountEmail(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cancel-user/{userId}/{token}")
    public ResponseEntity<Void> cancelUserAccount(@PathVariable long userId, @PathVariable @NotNull String token) {
        try {
            loginService.cancelUserAccount(userId, token);
        } catch (Exception e) {
            final URI uri = URI.create(serviceUrls.getSiteUrl() + "/erro-cancelamento");

            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .location(uri)
                    .build();
        }

        final URI uri = URI.create(serviceUrls.getSiteUrl() + "/conta-cancelada");

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(uri)
                .build();
    }
}
