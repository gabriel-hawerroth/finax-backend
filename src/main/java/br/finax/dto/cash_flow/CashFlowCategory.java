package br.finax.dto.cash_flow;

import java.util.List;

public record CashFlowCategory(
        long id,
        String name,
        String color,
        String icon,
        String type,
        List<CashFlowSubcategory> subcategories
) {
}
