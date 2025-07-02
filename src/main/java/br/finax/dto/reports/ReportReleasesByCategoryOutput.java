package br.finax.dto.reports;

import java.util.List;

public record ReportReleasesByCategoryOutput(
        List<ReleasesByCategory> releasesByCategories
) {
}
