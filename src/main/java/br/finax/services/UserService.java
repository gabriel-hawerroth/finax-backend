package br.finax.services;

import br.finax.models.User;
import br.finax.repository.UserRepository;
import br.finax.utils.UtilsService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCrypt;
    private final UtilsService utilsService;

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
    }

    public User getByEmail(String email) {
        try {
            return userRepository.findByEmail(email);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
        }
    }

    public ResponseEntity<User> changeForgetedPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (!user.getCanChangePassword())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Without permission to change the password");

        user.setPassword(bCrypt.encode(newPassword));
        user.setCanChangePassword(false);

        return ResponseEntity.status(HttpStatus.OK).body(userRepository.save(user));
    }

    public ResponseEntity<User> changePassword(String newPassword, String currentPassword) {
        User user = utilsService.getAuthUser();

        if (!bCrypt.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The current password is incorrect");
        }

        user.setPassword(bCrypt.encode(newPassword));

        return ResponseEntity.ok().body(userRepository.save(user));
    }

    public ResponseEntity<User> editUser(User user) {
        User existentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        existentUser.setFirstName(user.getFirstName());
        existentUser.setLastName(user.getLastName());

        return ResponseEntity.ok().body(userRepository.save(existentUser));
    }

    public ResponseEntity<User> changeUserImage(MultipartFile profileImage) throws IOException {
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
                user.setProfileImage(utilsService.compressImage(originalImage, false));
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().body(userRepository.save(user));
    }

    public ResponseEntity<byte[]> getUserImage() {
        byte[] compressedImage = userRepository.findById(utilsService.getAuthUser().getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST)).getProfileImage();

        return ResponseEntity.ok().body(compressedImage);
    }
}
