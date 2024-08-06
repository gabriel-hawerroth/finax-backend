package br.finax.controllers;

import br.finax.dto.EditUserDTO;
import br.finax.models.User;
import br.finax.services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    public final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable long id) {
        return ResponseEntity.ok(
                userService.findById(id)
        );
    }

    @GetMapping("/get-auth-user")
    public ResponseEntity<User> getAuthUser() {
        return ResponseEntity.ok(
                userService.getAuthUser()
        );
    }

    @PutMapping
    public ResponseEntity<User> editUser(@RequestBody @Valid EditUserDTO user) {
        return ResponseEntity.ok(
                userService.editUser(user)
        );
    }

    @PatchMapping("/change-forgeted-password")
    public ResponseEntity<User> changeForgetedPassword(@RequestParam long userId, @RequestParam @NotNull String newPassword) {
        return ResponseEntity.ok(
                userService.changeForgetedPassword(userId, newPassword)
        );
    }

    @PatchMapping("/change-password")
    public ResponseEntity<User> changePassword(@RequestParam @NotNull String newPassword, @RequestParam @NotNull String currentPassword) {
        return ResponseEntity.ok(
                userService.changePassword(newPassword, currentPassword)
        );
    }

    @PatchMapping("/change-profile-image")
    public ResponseEntity<User> changeUserImage(@RequestParam @NotNull MultipartFile file) {
        return ResponseEntity.ok(
                userService.changeUserImage(file)
        );
    }

    @GetMapping("/get-user-image")
    public ResponseEntity<String> getUserImage() {
        return ResponseEntity.ok(
                userService.getUserImage()
        );
    }
}
