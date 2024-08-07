package br.finax.services;

import br.finax.dto.EditUserDTO;
import br.finax.enums.ErrorCategory;
import br.finax.enums.S3FolderPath;
import br.finax.exceptions.CannotChangePasswordException;
import br.finax.exceptions.FileCompressionErrorException;
import br.finax.exceptions.FileIOException;
import br.finax.exceptions.InvalidPasswordException;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.ServiceException;
import br.finax.external.AwsS3Service;
import br.finax.models.User;
import br.finax.repository.UserRepository;
import br.finax.security.SecurityFilter;
import br.finax.utils.FileUtils;
import br.finax.utils.UtilsService;
import com.amazonaws.SdkClientException;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static br.finax.external.AwsS3Service.getS3FileName;
import static br.finax.utils.FileUtils.convertByteArrayToFile;
import static br.finax.utils.FileUtils.getFileExtension;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final AwsS3Service awsS3Service;
    private final SecurityFilter securityFilter;

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
        return UtilsService.getAuthUser();
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
        final User user = getAuthUser();

        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
            throw new InvalidPasswordException();

        user.setPassword(passwordEncoder.encode(newPassword));

        securityFilter.updateCachedUser(user);

        return userRepository.save(user);
    }

    @Transactional
    public User editUser(EditUserDTO userDTO) {
        final User user = getAuthUser();

        user.setFirstName(userDTO.firstName());
        user.setLastName(userDTO.lastName());

        securityFilter.updateCachedUser(user);

        return userRepository.save(user);
    }

    @Transactional
    public User changeUserImage(MultipartFile file) {
        final User user = getAuthUser();

        final String fileExtension = getFileExtension(file);
        final String fileName = getS3FileName(user.getId(), fileExtension, S3FolderPath.USER_PROFILE_IMG);

        try {
            final byte[] compressedFile = FileUtils.compressFile(file);

            final File tempFile = convertByteArrayToFile(compressedFile, fileName);

            try {
                if (user.getProfileImage() != null && !user.getProfileImage().isBlank())
                    awsS3Service.updateS3File(user.getProfileImage(), fileName, tempFile);
                else
                    awsS3Service.uploadS3File(fileName, tempFile);
            } finally {
                var _ = tempFile.delete();
            }

            user.setProfileImage(fileName);
            securityFilter.updateCachedUser(user);

            return userRepository.save(user);
        } catch (FileCompressionErrorException | FileIOException | SdkClientException e) {
            throw new ServiceException(ErrorCategory.INTERNAL_ERROR, "Failed to process the file", e);
        }
    }

    public String getUserImage() {
        return getAuthUser().getProfileImage();
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

    @Transactional
    public void activeUser(long userId) {
        userRepository.activeUser(userId);
    }
}
