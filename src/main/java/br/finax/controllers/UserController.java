package br.finax.controllers;

import br.finax.models.User;
import br.finax.services.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping
    private List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    private User getById(@PathVariable long id) {
        return userService.getById(id);
    }

    @GetMapping("/get-auth-user")
    private User getAuthUser() {
        return userService.getAuthUser();
    }

    @PutMapping("/change-forgeted-password")
    private ResponseEntity<User> changeForgetedPassword(@RequestParam Long userId, @RequestParam String newPassword) {
        return userService.changeForgetedPassword(userId, newPassword);
    }

    @PutMapping("/change-password")
    private ResponseEntity<User> changePassword(@RequestParam String newPassword, @RequestParam String currentPassword) {
       return userService.changePassword(newPassword, currentPassword);
    }

    @PutMapping
    private ResponseEntity<User> editUser(@RequestBody User user) {
        return userService.editUser(user);
    }

    @PutMapping("/change-profile-image")
    private ResponseEntity<User> changeUserImage(@RequestParam MultipartFile file) throws IOException {
        return userService.changeUserImage(file);
    }

    @GetMapping("/get-user-image")
    private ResponseEntity<byte[]> getUserImage() {
        return userService.getUserImage();
    }
}
