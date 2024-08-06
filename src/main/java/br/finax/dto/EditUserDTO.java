package br.finax.dto;

import lombok.NonNull;

public record EditUserDTO(@NonNull String firstName, String lastName) {
}
