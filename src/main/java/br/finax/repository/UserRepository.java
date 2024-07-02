package br.finax.repository;

import br.finax.models.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(@NonNull String email);

    boolean existsByEmail(String email);
}
