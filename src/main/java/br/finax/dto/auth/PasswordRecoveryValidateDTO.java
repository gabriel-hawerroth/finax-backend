package br.finax.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PasswordRecoveryValidateDTO(
        @NotNull
        @NotBlank
        String token
) {
}
