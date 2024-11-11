package br.finax.repository;

import br.finax.models.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(@NonNull String email);

    boolean existsByEmail(@NonNull String email);

    @Modifying
    @Query("""
            UPDATE User u
            SET u.active = true
            WHERE u.id = ?1
            """)
    void activeUser(long userId);

    @Query("""
            SELECT u.profileImage
            FROM User u
            WHERE
                u.profileImage is not null
                AND u.profileImage <> ''
            """)
    List<String> getAllUserProfileImages();
}
