package br.finax.events.user_created;

import br.finax.models.User;

public record UserCreatedEvent(
        User user
) {
}
