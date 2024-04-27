package br.finax.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmailDTO(
        @Email
        @NotNull
        @NotBlank
        String addressee,

        @NotNull
        @NotBlank
        String subject,

        @NotNull
        @NotBlank
        String content
) {
}
