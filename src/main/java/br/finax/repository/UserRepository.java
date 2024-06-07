package br.finax.repository;

import br.finax.models.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(@NonNull String email);

    @Modifying
    @Query("update User u set u.password = ?2 where u.id = ?1")
    void updatePassword(@NonNull Long userId, @NonNull String newPassword);

    @Modifying
    @Query("update User u set u.profileImage = ?2 where u.id = ?1")
    void updateProfileImage(@NonNull Long id, byte[] newProfileImage);
}
