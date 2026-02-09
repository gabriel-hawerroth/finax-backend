package br.finax.dto;

import br.finax.models.Category;

public record SaveCategoryDTO(
        String name,
        String color,
        String icon,
        String type,
        boolean essential
) {
    public Category toEntity() {
        return new Category(name, color, icon, type, essential);
    }
}
