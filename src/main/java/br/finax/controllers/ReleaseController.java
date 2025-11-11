package br.finax.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.finax.dto.cash_flow.CashFlowValues;
import br.finax.dto.cash_flow.MonthlyRelease;
import br.finax.enums.release.DuplicatedReleaseAction;
import br.finax.models.Release;
import br.finax.services.ReleaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/cash-flow")
public class ReleaseController {

    public final ReleaseService releaseService;

    @GetMapping("/get-monthly-releases")
    public ResponseEntity<List<MonthlyRelease>> getMonthlyReleases(
            @RequestParam String monthYear // should be in yyyy-MM format
    ) {
        return ResponseEntity.ok(
                releaseService.getMonthlyReleases(monthYear)
        );
    }

    @GetMapping("/get-values")
    public ResponseEntity<CashFlowValues> getValues() {
        return ResponseEntity.ok(
                releaseService.getValues()
        );
    }

    @PostMapping
    public ResponseEntity<Release> addRelease(
            @RequestBody @Valid Release release,
            @RequestParam int repeatFor
    ) {
        final Release cashFlow = releaseService.addRelease(release, repeatFor);

        final URI uri = URI.create("/cash-flow/" + cashFlow.getId());

        return ResponseEntity
                .created(uri)
                .body(cashFlow);
    }

    @PutMapping
    public ResponseEntity<Release> editRelease(
            @RequestBody @Valid Release release,
            @RequestParam String duplicatedReleaseAction
    ) {
        return ResponseEntity.ok(
                releaseService.editRelease(
                        release, DuplicatedReleaseAction.valueOf(duplicatedReleaseAction)
                )
        );
    }

    @PatchMapping("/add-attachment/{id}")
    public ResponseEntity<Release> addAttachment(@PathVariable long id, @RequestParam MultipartFile file) {
        return ResponseEntity.ok(
                releaseService.saveAttachment(id, file)
        );
    }

    @PatchMapping("/remove-attachment/{id}")
    public ResponseEntity<Release> removeAttachment(@PathVariable long id) {
        return ResponseEntity.ok(
                releaseService.removeAttachment(id)
        );
    }

    @GetMapping("/get-attachment/{id}")
    public ResponseEntity<byte[]> getAttachment(@PathVariable long id) {
        return ResponseEntity.ok(
                releaseService.getAttachment(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id, @RequestParam String duplicatedReleasesAction) {
        releaseService.delete(id, DuplicatedReleaseAction.valueOf(duplicatedReleasesAction));

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/update-done/{id}")
    public ResponseEntity<Void> updateDone(
            @PathVariable long id,
            @RequestParam boolean done
    ) {
        releaseService.updateDone(id, done);

        return ResponseEntity.noContent().build();
    }
}
