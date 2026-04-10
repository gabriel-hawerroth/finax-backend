package br.finax.controllers;

import br.finax.dto.auth.PasswordRecoveryConfirmDTO;
import br.finax.dto.auth.PasswordRecoveryRequestDTO;
import br.finax.dto.auth.PasswordRecoveryValidateDTO;
import br.finax.exceptions.ExpiredLinkException;
import br.finax.services.LoginService;
import br.finax.services.PasswordRecoveryService;
import br.finax.utils.ServiceUrls;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    private final PasswordRecoveryService passwordRecoveryService;
    private final ServiceUrls serviceUrls;

    @GetMapping("/activate-account/{userId}/{token}")
    public ResponseEntity<Void> activateUser(@PathVariable Long userId, @PathVariable @NotNull String token) {
        URI uri;

        try {
            loginService.activateUser(userId, token);

            uri = URI.create(serviceUrls.getSiteUrl() + "/ativacao-da-conta");
        } catch (Exception e) {
            final String siteUrl = e instanceof ExpiredLinkException
                    ? "/link-expirado"
                    : "/erro-ativacao";

            uri = URI.create(serviceUrls.getSiteUrl() + siteUrl);
        }

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(uri)
                .build();
    }

    @PostMapping("/send-cancel-account-email/{userId}")
    public ResponseEntity<Void> sendCancelUserAccountEmail(@PathVariable long userId) {
        loginService.sendCancelUserAccountEmail(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-recovery/request")
    public ResponseEntity<Void> requestPasswordRecovery(
            @RequestBody @Valid PasswordRecoveryRequestDTO body,
            HttpServletRequest request
    ) {
        passwordRecoveryService.requestRecovery(body.email(), request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/password-recovery/confirm")
    public ResponseEntity<Void> confirmPasswordRecovery(@RequestBody @Valid PasswordRecoveryConfirmDTO body) {
        passwordRecoveryService.confirmRecovery(body.token(), body.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-recovery/validate")
    public ResponseEntity<Void> validatePasswordRecoveryToken(@RequestBody @Valid PasswordRecoveryValidateDTO body) {
        passwordRecoveryService.validateToken(body.token());
        return ResponseEntity.noContent().build();
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
