package br.finax.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveSubcategoryDTO(
        @NotBlank
        String name,

        boolean essential,

        @NotNull
        Long categoryId
) {
}

