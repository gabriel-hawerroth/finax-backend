package br.finax.controllers;

import br.finax.dto.reports.BalanceEvolutionItem;
import br.finax.dto.reports.ReleasesByAccount;
import br.finax.dto.reports.ReleasesByCategory;
import br.finax.enums.release.ReleaseType;
import br.finax.enums.reports.BalanceEvolutionGrouper;
import br.finax.enums.reports.ReportReleasesByInterval;
import br.finax.services.ReportsService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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
    public ResponseEntity<List<ReleasesByCategory>> getReleasesByCategory(
            @RequestParam ReportReleasesByInterval interval,
            @RequestParam ReleaseType releaseType,
            @RequestParam(required = false) LocalDate initialDate,
            @RequestParam(required = false) LocalDate finalDate
    ) {
        return ResponseEntity.ok(
                reportsService.getReleasesByCategory(interval, releaseType, initialDate, finalDate)
        );
    }

    @GetMapping("/releases-by-account")
    public ResponseEntity<List<ReleasesByAccount>> getReleasesByAccount(
            @RequestParam ReportReleasesByInterval interval,
            @RequestParam ReleaseType releaseType,
            @RequestParam(required = false) LocalDate initialDate,
            @RequestParam(required = false) LocalDate finalDate
    ) {
        return ResponseEntity.ok(
                reportsService.getReleasesByAccount(interval, releaseType, initialDate, finalDate)
        );
    }

    @GetMapping("/balance-evolution")
    public ResponseEntity<List<BalanceEvolutionItem>> getBalanceEvolution(
            @RequestParam ReportReleasesByInterval interval,
            @RequestParam(required = false) LocalDate initialDate,
            @RequestParam(required = false) LocalDate finalDate,
            @RequestParam(required = false) Long accountId,
            @RequestParam BalanceEvolutionGrouper grouper
    ) {
        return ResponseEntity.ok(
                reportsService.getBalanceEvolution(interval, initialDate, finalDate, accountId, grouper)
        );
    }
}
