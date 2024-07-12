package br.finax.controllers;

import br.finax.dto.CashFlowValues;
import br.finax.dto.MonthlyCashFlow;
import br.finax.enums.DuplicatedReleaseAction;
import br.finax.models.CashFlow;
import br.finax.services.CashFlowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cash-flow")
public class CashFlowController {

    public final CashFlowService cashFlowService;

    @GetMapping
    public ResponseEntity<MonthlyCashFlow> getMonthlyFlow(
            @RequestParam LocalDate firstDt, @RequestParam LocalDate lastDt
    ) {
        return ResponseEntity.ok(
                cashFlowService.getMonthlyFlow(firstDt, lastDt)
        );
    }

    @GetMapping("/get-values")
    public ResponseEntity<CashFlowValues> getValues() {
        return ResponseEntity.ok(
                cashFlowService.getValues()
        );
    }

    @PostMapping
    public ResponseEntity<CashFlow> addRelease(
            @RequestBody @Valid CashFlow release,
            @RequestParam int repeatFor
    ) {
        final CashFlow cashFlow = cashFlowService.addRelease(release, repeatFor);

        final URI uri = URI.create("/cash-flow/" + cashFlow.getId());

        return ResponseEntity.created(uri).body(
                cashFlow
        );
    }

    @PutMapping
    public ResponseEntity<CashFlow> editRelease(
            @RequestBody @Valid CashFlow release,
            @RequestParam String duplicatedReleaseAction
    ) {
        return ResponseEntity.ok(
                cashFlowService.editRelease(
                        release, DuplicatedReleaseAction.valueOf(duplicatedReleaseAction)
                )
        );
    }

    @PatchMapping("/add-attachment/{id}")
    public ResponseEntity<CashFlow> addAttachment(@PathVariable long id, @RequestParam MultipartFile file) {
        return ResponseEntity.ok(
                cashFlowService.addAttachment(id, file)
        );
    }

    @PatchMapping("/remove-attachment/{id}")
    public ResponseEntity<CashFlow> removeAttachment(@PathVariable long id) {
        return ResponseEntity.ok(
                cashFlowService.removeAttachment(id)
        );
    }

    @GetMapping("/get-attachment/{id}")
    public ResponseEntity<byte[]> getAttachment(@PathVariable long id) {
        return ResponseEntity.ok(
                cashFlowService.getAttachment(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id, @RequestParam String duplicatedReleasesAction) {
        cashFlowService.delete(id, DuplicatedReleaseAction.valueOf(duplicatedReleasesAction));

        return ResponseEntity.ok().build();
    }
}
