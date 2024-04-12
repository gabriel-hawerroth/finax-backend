package br.finax.dto;

public record EmailRecord(
        String addressee,
        String subject,
        String content
) {
}
