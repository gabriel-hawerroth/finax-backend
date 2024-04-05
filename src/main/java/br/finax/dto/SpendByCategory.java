package br.finax.dto;

import br.finax.models.Category;

public record SpendByCategory(
        Category category,
        double percent,
        double value
) {}
