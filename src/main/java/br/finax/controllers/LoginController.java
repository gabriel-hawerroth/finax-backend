package br.finax.controllers;

import br.finax.services.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginService loginService;

    @GetMapping("/activate-account/{userId}/{token}")
    public ResponseEntity<Void> activateUser(@PathVariable Long userId, @PathVariable String token) {
        return loginService.activateUser(userId, token);
    }

    @PostMapping("/send-change-password-email")
    public void sendChangePasswordMail(@RequestParam String email) {
        loginService.sendChangePasswordMail(email);
    }

    @GetMapping("/permit-change-password/{userId}/{token}")
    public ResponseEntity<Void> permitChangePassword(@PathVariable Long userId, @PathVariable String token) {
        return loginService.permitChangePassword(userId, token);
    }
}
