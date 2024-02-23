package br.finax.repository;

import br.finax.models.UserConfigs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserConfigsRepository extends JpaRepository<UserConfigs, Long> {

    Optional<UserConfigs> findByUserId(Long userId);
}
