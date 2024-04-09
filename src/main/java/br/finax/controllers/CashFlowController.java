package br.finax.controllers;

import br.finax.dto.MonthlyCashFlow;
import br.finax.enums.ReleasesViewMode;
import br.finax.enums.DuplicatedReleaseAction;
import br.finax.models.CashFlow;
import br.finax.dto.CashFlowValues;
import br.finax.services.CashFlowService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cash-flow")
public class CashFlowController {

    private final CashFlowService cashFlowService;

    @GetMapping
    private MonthlyCashFlow getMonthlyFlow(
            @RequestParam Date firstDt, @RequestParam Date lastDt,
            @RequestParam String viewMode, @RequestParam Date firstDtCurrentMonth,
            @RequestParam Date firstDtInvoice, @RequestParam Date lastDtInvoice
    ) {
        return cashFlowService.getMonthlyFlow(
                firstDt, lastDt, ReleasesViewMode.valueOf(viewMode), firstDtCurrentMonth, firstDtInvoice, lastDtInvoice
        );
    }

    @GetMapping("/get-values")
    private CashFlowValues getValues() {
        return cashFlowService.getValues();
    }

    @PostMapping
    private ResponseEntity<CashFlow> addRelease(
            @RequestBody CashFlow release, @RequestParam int repeatFor
    ) {
        return cashFlowService.addRelease(release, repeatFor);
    }

    @PutMapping
    private ResponseEntity<CashFlow> editRelease(
            @RequestBody CashFlow release,
            @RequestParam String duplicatedReleaseAction
    ) {
        return cashFlowService.editRelease(
                release, DuplicatedReleaseAction.valueOf(duplicatedReleaseAction));
    }

    @PutMapping("/add-attachment/{id}")
    private ResponseEntity<CashFlow> addAttachment(@PathVariable long id, @RequestParam MultipartFile file) {
        return cashFlowService.addAttachment(id, file);
    }

    @DeleteMapping("/remove-attachment/{id}")
    private ResponseEntity<CashFlow> removeAttachment(@PathVariable long id) {
        return cashFlowService.removeAttachment(id);
    }

    @GetMapping("/get-attachment/{id}")
    private ResponseEntity<byte[]> getAttachment(@PathVariable long id) {
        return cashFlowService.getAttachment(id);
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<?> delete(@PathVariable long id, @RequestParam String duplicatedReleasesAction) {
        return cashFlowService.delete(id, DuplicatedReleaseAction.valueOf(duplicatedReleasesAction));
    }
}
