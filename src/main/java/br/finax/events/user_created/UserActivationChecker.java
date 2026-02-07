package br.finax.events.user_created;

import br.finax.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserActivationChecker {

    private final UserRepository userRepository;

    @Transactional
    public void checkAndDeleteInactiveUser(Long userId) {
        userRepository.findById(userId)
                .ifPresent(user -> {
                    if (!user.isActive())
                        userRepository.delete(user);
                });
    }
}


