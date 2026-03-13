package br.finax.dto.user;

import lombok.NonNull;

public record EditUserDTO(@NonNull String firstName, String lastName) {
}
