package br.finax.dto.reports;

import br.finax.dto.home.SpendByCategory;

import java.util.List;

public record ReportReleasesByCategoryOutput(
        List<SpendByCategory> spendByCategories
) {
}
