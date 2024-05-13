package br.finax.controllers;

import br.finax.models.User;
import br.finax.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    public final UserService userService;

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable long id) {
        return userService.getById(id);
    }

    @GetMapping("/get-auth-user")
    public User getAuthUser() {
        return userService.getAuthUser();
    }

    @PutMapping("/change-forgeted-password")
    public ResponseEntity<User> changeForgetedPassword(@RequestParam Long userId, @RequestParam String newPassword) {
        return userService.changeForgetedPassword(userId, newPassword);
    }

    @PutMapping("/change-password")
    public ResponseEntity<User> changePassword(@RequestParam String newPassword, @RequestParam String currentPassword) {
        return userService.changePassword(newPassword, currentPassword);
    }

    @PutMapping
    public ResponseEntity<User> editUser(@RequestBody @Valid User user) {
        return userService.editUser(user);
    }

    @PutMapping("/change-profile-image")
    public ResponseEntity<User> changeUserImage(@RequestParam MultipartFile file) throws IOException {
        return userService.changeUserImage(file);
    }

    @GetMapping("/get-user-image")
    public ResponseEntity<byte[]> getUserImage() {
        return userService.getUserImage();
    }
}
