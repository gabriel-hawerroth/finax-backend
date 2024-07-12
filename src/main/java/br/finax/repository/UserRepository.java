package br.finax.repository;

import br.finax.models.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(@NonNull String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("""
                update User u
                set u.active = true
                where u.id = ?1
            """)
    void activeUser(long userId);
}
