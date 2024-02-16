package br.finax.controllers;

import br.finax.models.User;
import br.finax.services.LoginService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/new-user")
    private ResponseEntity<User> newUser(@RequestBody User user) {
        return loginService.newUser(user);
    }

    @GetMapping("/activate-account/{userId}/{token}")
    private ResponseEntity<Void> activateUser(@PathVariable Long userId, @PathVariable String token) {
        return loginService.activateUser(userId, token);
    }

    @PostMapping("/send-change-password-email")
    private void sendChangePasswordMail(@RequestParam String email) {
        loginService.sendChangePasswordMail(email);
    }

    @GetMapping("/permit-change-password/{userId}/{token}")
    private ResponseEntity<Void> permitChangePassword(@PathVariable Long userId, @PathVariable String token) {
        return loginService.permitChangePassword(userId, token);
    }
}
