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

    public User getAuthUser() {
        return utilsService.getAuthUser();
    }

    public ResponseEntity<User> changeForgetedPassword(Long userId, String newPassword) {
       final User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (!user.isCanChangePassword())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Without permission to change the password");

        user.setPassword(bCrypt.encode(newPassword));
        user.setCanChangePassword(false);

        return ResponseEntity.status(HttpStatus.OK).body(userRepository.save(user));
    }

    public ResponseEntity<User> changePassword(String newPassword, String currentPassword) {
        final User user = utilsService.getAuthUser();

        if (!bCrypt.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The current password is incorrect");
        }

        user.setPassword(bCrypt.encode(newPassword));

        return ResponseEntity.ok().body(userRepository.save(user));
    }

    public ResponseEntity<User> editUser(User user) {
        final User existentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        existentUser.setFirstName(user.getFirstName());
        existentUser.setLastName(user.getLastName());

        return ResponseEntity.ok().body(userRepository.save(existentUser));
    }

    public ResponseEntity<User> changeUserImage(MultipartFile file) throws IOException {
        final User user = userRepository.findById(utilsService.getAuthUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        if (file.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        byte[] image = file.getBytes();

        final String imgExtension = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[1];

        if (!imgExtension.equals("png") && !imgExtension.equals("webp")) {
            try {
                image = utilsService.compressImage(image, false);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        user.setProfileImage(image);

        return ResponseEntity.ok().body(userRepository.save(user));
    }

    public ResponseEntity<byte[]> getUserImage() {
        return ResponseEntity.ok().body(utilsService.getAuthUser().getProfileImage());
    }
}
