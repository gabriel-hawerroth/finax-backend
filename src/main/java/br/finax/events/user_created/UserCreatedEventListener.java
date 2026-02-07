package br.finax.events.user_created;

import br.finax.events.TaskSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class UserCreatedEventListener {

    private final TaskSchedulerService taskSchedulerService;
    private final UserActivationChecker userActivationChecker;

    @Async
    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        final var executionTime = Instant.now().plus(2, ChronoUnit.HOURS).plus(2, ChronoUnit.MINUTES);

        taskSchedulerService.scheduleTask(() -> userActivationChecker.checkAndDeleteInactiveUser(event.user().getId()), executionTime);
    }
}
