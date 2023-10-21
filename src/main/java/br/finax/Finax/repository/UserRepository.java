package br.finax.finax.repository;

import br.finax.finax.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String usuario);
}
