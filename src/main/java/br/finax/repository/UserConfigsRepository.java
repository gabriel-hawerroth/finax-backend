package br.finax.repository;

import br.finax.models.UserConfigs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConfigsRepository extends JpaRepository<UserConfigs, Long> {

    UserConfigs findByUserId(Long userId);
}
