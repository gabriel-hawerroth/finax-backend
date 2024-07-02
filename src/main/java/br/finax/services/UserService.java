package br.finax.services;

import br.finax.enums.ErrorCategory;
import br.finax.enums.S3FolderPath;
import br.finax.exceptions.CannotChangePasswordException;
import br.finax.exceptions.InvalidPasswordException;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.ServiceException;
import br.finax.models.User;
import br.finax.repository.UserRepository;
import br.finax.security.SecurityFilter;
import br.finax.utils.FileUtils;
import br.finax.utils.UtilsService;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static br.finax.utils.FileUtils.convertByteArrayToFile;
import static br.finax.utils.FileUtils.getFileExtension;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final AwsS3Service awsS3Service;
    private final SecurityFilter securityFilter;
    private final UtilsService utils;
    private final FileUtils fileUtils;

    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User findByEmail(@NotNull String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional(readOnly = true)
    public User findById(long id) {
        return userRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    public User getAuthUser() {
        return utils.getAuthUser();
    }

    @Transactional
    public User changeForgetedPassword(long userId, @NonNull String newPassword) {
        final User user = findById(userId);

        if (!user.isCanChangePassword())
            throw new CannotChangePasswordException();

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCanChangePassword(false);

        securityFilter.updateCachedUser(user);

        return userRepository.save(user);
    }

    @Transactional
    public User changePassword(String newPassword, String currentPassword) {
        final User user = utils.getAuthUser();

        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
            throw new InvalidPasswordException();

        user.setPassword(passwordEncoder.encode(newPassword));

        securityFilter.updateCachedUser(user);

        return userRepository.save(user);
    }

    @Transactional
    public User editUser(User userDTO) {
        final User user = utils.getAuthUser();

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());

        securityFilter.updateCachedUser(user);

        return userRepository.save(user);
    }

    @Transactional
    public User changeUserImage(MultipartFile file) {
        final User user = utils.getAuthUser();

        final String fileExtension = getFileExtension(file);
        final String fileName = awsS3Service.getS3FileName(user.getId(), fileExtension, S3FolderPath.USER_PROFILE_IMG);

        try (final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            final Future<byte[]> compressedFileFuture = executor.submit(() -> fileUtils.compressFile(file));
            final byte[] compressedFile = compressedFileFuture.get();

            final Future<File> tempFileFuture = executor.submit(() -> convertByteArrayToFile(compressedFile, fileName));
            final File tempFile = tempFileFuture.get();

            if (user.getProfileImage() != null && !user.getProfileImage().isBlank())
                executor.submit(() -> awsS3Service.updateS3File(user.getProfileImage(), fileName, tempFile)).get();
            else
                executor.submit(() -> awsS3Service.uploadS3File(fileName, tempFile)).get();

            var _ = tempFile.delete();

            user.setProfileImage(fileName);
            securityFilter.updateCachedUser(user);

            return userRepository.save(user);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException(ErrorCategory.INTERNAL_ERROR, e.getMessage(), e);
        }
    }

    public String getUserImage() {
        return utils.getAuthUser().getProfileImage();
    }

    @Transactional
    public User save(User user) {
        securityFilter.updateCachedUser(user);
        return userRepository.save(user);
    }

    @Transactional
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
