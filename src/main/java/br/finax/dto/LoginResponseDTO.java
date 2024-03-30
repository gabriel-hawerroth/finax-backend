package br.finax.dto;

import br.finax.models.User;

public record LoginResponseDTO(User user, String token) {
}
