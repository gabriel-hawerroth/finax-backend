package br.finax.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthenticationDTO(
        @NotNull
        @NotBlank
        String login,

        @NotNull
        @NotBlank
        String password
) {
}
