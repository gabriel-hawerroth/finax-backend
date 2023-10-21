package br.finax.finax.controllers;

import br.finax.finax.models.User;
import br.finax.finax.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCrypt;

    @GetMapping
    private List<User> getUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    private User getById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado"));
    }

    @GetMapping("/get-by-email")
    private User getByEmail(@RequestParam String email) {
        try {
            return userRepository.findByEmail(email);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado");
        }
    }

    @PutMapping("/change-forgeted-password")
    private ResponseEntity<User> changeForgetedPassword(@RequestParam Long userId, @RequestParam String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (!user.getCanChangePassword()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sem permissão para alterar a senha");
        }

        user.setPassword(bCrypt.encode(newPassword));
        user.setCanChangePassword(false);

        return ResponseEntity.status(HttpStatus.OK).body(userRepository.save(user));
    }

    @PutMapping("/change-password")
    private ResponseEntity<User> changePassword(
            @RequestParam Long userId,
            @RequestParam String newPassword,
            @RequestParam String currentPassword
    ) {
       User user = userRepository.findById(userId)
               .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

       if (!bCrypt.matches(currentPassword, user.getPassword())) {
           throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
       }

       user.setPassword(bCrypt.encode(newPassword));

       return ResponseEntity.ok().body(userRepository.save(user));
    }

    @PutMapping
    private ResponseEntity<User> editUser(@RequestBody User user) {
        User existentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        existentUser.setFirstName(user.getFirstName());
        existentUser.setLastName(user.getLastName());

        return ResponseEntity.ok().body(userRepository.save(existentUser));
    }

    @PutMapping("/change-profile-image/{userId}")
    private ResponseEntity<User> changeUserImage(@PathVariable Long userId,
                  @RequestParam("file") MultipartFile profileImage) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (profileImage.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        try {
            user.setProfileImage(profileImage.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().body(userRepository.save(user));
    }

    @GetMapping("/get-user-image/{userId}")
    private ResponseEntity<byte[]> getUserImage(@PathVariable Long userId) throws IOException {
        if (userId == null) return null;

        return ResponseEntity.ok().body(userRepository.findById(userId).get().getProfileImage());
    }
}
