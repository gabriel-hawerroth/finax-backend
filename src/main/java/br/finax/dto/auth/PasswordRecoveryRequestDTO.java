package br.finax.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PasswordRecoveryRequestDTO(
        @NotNull
        @NotBlank
        @Email
        String email
) {
}
