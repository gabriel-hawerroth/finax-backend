package br.finax.finax.repository;

import br.finax.finax.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountsRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByUserIdOrderByPresentationSequenceAsc(Long userId);
}
