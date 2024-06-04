package br.finax.services;

import br.finax.exceptions.CannotChangePasswordException;
import br.finax.exceptions.InvalidPasswordException;
import br.finax.exceptions.NotFoundException;
import br.finax.models.User;
import br.finax.repository.UserRepository;
import br.finax.security.SecurityFilter;
import br.finax.utils.FileUtils;
import br.finax.utils.UtilsService;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final SecurityFilter securityFilter;
    private final UtilsService utils;
    private final FileUtils fileUtils;

    private final PasswordEncoder passwordEncoder;

    public User findByEmail(@NotNull String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(NotFoundException::new);
    }

    public User findById(long id) {
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

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCanChangePassword(false);

        securityFilter.updateCachedUser(user);

        return userRepository.save(user);
    }

    public User changePassword(String newPassword, String currentPassword) {
        final User user = utils.getAuthUser();

        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
            throw new InvalidPasswordException();

        newPassword = passwordEncoder.encode(newPassword);

        userRepository.updatePassword(user.getId(), newPassword);

        return findById(user.getId());
    }

    public User editUser(User userDTO) {
        final User user = userRepository.findById(userDTO.getId())
                .orElseThrow(NotFoundException::new);

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());

        securityFilter.updateCachedUser(user);
        return userRepository.save(user);
    }

    @Transactional
    public User changeUserImage(MultipartFile file) {
        final long userId = utils.getAuthUser().getId();

        final byte[] compressedFile = fileUtils.compressFile(file);
        userRepository.updateProfileImage(userId, compressedFile);

        final User user = findById(userId);
        securityFilter.updateCachedUser(user);

        return user;
    }

    public byte[] getUserImage() {
        return utils.getAuthUser().getProfileImage();
    }

    public User save(User user) {
        securityFilter.updateCachedUser(user);
        return userRepository.save(user);
    }
}
