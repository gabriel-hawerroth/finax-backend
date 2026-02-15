package br.finax.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GoogleAuthDTO(
        @NotNull
        @NotBlank
        String credential
) {
}
