package br.finax.dto;

public record EmailDTO(
        String addressee,
        String subject,
        String content
) {
}
