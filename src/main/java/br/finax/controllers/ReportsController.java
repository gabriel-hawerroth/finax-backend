package br.finax.controllers;

import br.finax.dto.reports.ReportReleasesByCategoryOutput;
import br.finax.enums.release.ReleaseType;
import br.finax.enums.reports.ReportReleasesByInterval;
import br.finax.services.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/releases-by-category")
    public ResponseEntity<ReportReleasesByCategoryOutput> getReleasesByCategory(
            @RequestParam ReportReleasesByInterval interval,
            @RequestParam ReleaseType releaseType,
            @RequestParam(required = false) String monthYear
    ) {
        return ResponseEntity.ok(
                reportsService.getReleasesByCategory(interval, releaseType, monthYear)
        );
    }
}
