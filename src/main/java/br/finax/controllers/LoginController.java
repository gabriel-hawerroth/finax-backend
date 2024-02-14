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
    private ResponseEntity<Void> activaUser(@PathVariable Long userId, @PathVariable String token) {
        return loginService.activaUser(userId, token);
    }

    @PostMapping("/send-change-password-email")
    private void requestPermissionToChangePassword(@RequestParam String email) {
        loginService.requestPermissionToChangePassword(email);
    }

    @GetMapping("/permit-change-password/{userId}/{token}")
    private ResponseEntity<Void> permitChangePassword(@PathVariable Long userId, @PathVariable String token) {
        return loginService.permitChangePassword(userId, token);
    }
}
