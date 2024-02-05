package br.finax.controllers;

import br.finax.enums.ImgFormat;
import br.finax.models.User;
import br.finax.repository.UserRepository;
import br.finax.utils.UtilsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCrypt;
    @Autowired
    private UtilsService utilsService;

    @GetMapping
    private List<User> getUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    private User getById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
    }

    @GetMapping("/get-by-email")
    private User getByEmail(@RequestParam String email) {
        try {
            return userRepository.findByEmail(email);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
        }
    }

    @PutMapping("/change-forgeted-password")
    private ResponseEntity<User> changeForgetedPassword(@RequestParam Long userId, @RequestParam String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (!user.getCanChangePassword())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Without permission to change the password");

        user.setPassword(bCrypt.encode(newPassword));
        user.setCanChangePassword(false);

        return ResponseEntity.status(HttpStatus.OK).body(userRepository.save(user));
    }

    @PutMapping("/change-password")
    private ResponseEntity<User> changePassword(
            @RequestParam String newPassword,
            @RequestParam String currentPassword
    ) {
       User user = utilsService.getAuthUser();

       if (!bCrypt.matches(currentPassword, user.getPassword())) {
           throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The current password is incorrect");
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

    @PutMapping("/change-profile-image")
    private ResponseEntity<User> changeUserImage(@RequestParam("file") MultipartFile profileImage) throws IOException {
        User user = userRepository.findById(utilsService.getAuthUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        if (profileImage.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        byte[] originalImage = profileImage.getBytes();

        String imgExtension = Objects.requireNonNull(profileImage.getOriginalFilename()).split("\\.")[1];

        try {
            // Resize and compress the image before saving
            if (imgExtension.equals("png") || imgExtension.equals("webp")) {
                user.setProfileImage(originalImage);
            } else {
                user.setProfileImage(utilsService.compressImage(originalImage, false, ImgFormat.JPG));
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().body(userRepository.save(user));
    }

    @Cacheable
    @GetMapping("/get-user-image")
    private ResponseEntity<byte[]> getUserImage() {
        byte[] compressedImage = userRepository.findById(utilsService.getAuthUser().getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST)).getProfileImage();

        return ResponseEntity.ok().body(compressedImage);
    }
}
