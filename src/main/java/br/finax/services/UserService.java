package br.finax.services;

import br.finax.exceptions.*;
import br.finax.models.User;
import br.finax.repository.UserRepository;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCrypt;
    private final UtilsService utils;

    public User getById(long id) {
        return userRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    public User getAuthUser() {
        return utils.getAuthUser();
    }

    public User changeForgetedPassword(Long userId, String newPassword) {
        final User user = userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);

        if (!user.isCanChangePassword())
            throw new CannotChangePasswordException();

        user.setPassword(bCrypt.encode(newPassword));
        user.setCanChangePassword(false);

        return userRepository.save(user);
    }

    public User changePassword(String newPassword, String currentPassword) {
        final User user = utils.getAuthUser();

        if (!bCrypt.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException();
        }

        user.setPassword(bCrypt.encode(newPassword));

        return userRepository.save(user);
    }

    public User editUser(User user) {
        final User existentUser = userRepository.findById(user.getId())
                .orElseThrow(NotFoundException::new);

        existentUser.setFirstName(user.getFirstName());
        existentUser.setLastName(user.getLastName());

        return userRepository.save(existentUser);
    }

    public User changeUserImage(MultipartFile file) throws IOException {
        final User user = userRepository.findById(utils.getAuthUser().getId())
                .orElseThrow(NotFoundException::new);

        if (file.isEmpty())
            throw new EmptyFileException();

        byte[] image = file.getBytes();

        final String imgExtension = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[1];

        if (!imgExtension.equals("png") && !imgExtension.equals("webp")) {
            try {
                image = utils.compressImage(image, false);
            } catch (IOException e) {
                throw new CompressionErrorException();
            }
        }

        user.setProfileImage(image);

        return userRepository.save(user);
    }

    public byte[] getUserImage() {
        return utils.getAuthUser().getProfileImage();
    }
}
