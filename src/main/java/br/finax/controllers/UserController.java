package br.finax.controllers;

import br.finax.dto.user.EditUserDTO;
import br.finax.models.User;
import br.finax.services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    public final UserService userService;

    @PutMapping
    public ResponseEntity<User> editUser(@RequestBody @Valid EditUserDTO user) {
        return ResponseEntity.ok(
                userService.editUser(user)
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
        final String userImage = userService.getUserImage();

        return isNotBlank(userImage)
                ? ResponseEntity.ok(userImage)
                : ResponseEntity.noContent().build();
    }
}
