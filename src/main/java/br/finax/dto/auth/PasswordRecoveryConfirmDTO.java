package br.finax.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PasswordRecoveryConfirmDTO(
        @NotNull
        @NotBlank
        String token,

        @NotNull
        @NotBlank
        String newPassword
) {
}
