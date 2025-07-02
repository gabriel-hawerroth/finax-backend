package br.finax.dto.reports;

import java.util.List;

public record ReportReleasesByAccountOutput(
        List<ReleasesByAccount> releasesByAccounts
) {
}
