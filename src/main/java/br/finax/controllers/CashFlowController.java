package br.finax.controllers;

import br.finax.enums.DuplicatedReleaseAction;
import br.finax.enums.ReleasedOn;
import br.finax.models.CashFlow;
import br.finax.records.MontlhyCashFlow;
import br.finax.services.CashFlowService;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cash-flow")
public class CashFlowController {

    private final CashFlowService cashFlowService;

    @Cacheable
    @GetMapping
    private MontlhyCashFlow getMonthlyFlow(@RequestParam Date firstDt,
                               @RequestParam Date lastDt, @RequestParam Date firstDtCurrentMonth) {
        return cashFlowService.getMonthlyFlow(firstDt, lastDt, firstDtCurrentMonth);
    }

    @PostMapping
    private ResponseEntity<CashFlow> addRelease(@RequestBody CashFlow release,
                                @RequestParam String releasedOn, @RequestParam int repeatFor) {
        return cashFlowService.addRelease(release, ReleasedOn.valueOf(releasedOn), repeatFor);
    }

    @PutMapping
    private ResponseEntity<CashFlow> editRelease(@RequestBody CashFlow release,
                                 @RequestParam String releasedOn, @RequestParam String duplicatedReleaseAction) {
        return cashFlowService.editRelease(release, ReleasedOn.valueOf(releasedOn), DuplicatedReleaseAction.valueOf(duplicatedReleaseAction));
    }

    @PutMapping("/add-attachment/{id}")
    private ResponseEntity<CashFlow> addAttachment(@PathVariable long id, @RequestParam MultipartFile file) {
        return cashFlowService.addAttachment(id, file);
    }

    @PutMapping("/remove-attachment/{id}")
    private ResponseEntity<CashFlow> removeAttachment(@PathVariable long id) {
        return cashFlowService.removeAttachment(id);
    }

    @Cacheable
    @GetMapping("/get-attachment/{id}")
    private ResponseEntity<byte[]> getAttachment(@PathVariable long id) {
        return cashFlowService.getAttachment(id);
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<?> delete(@PathVariable long id, @RequestParam String duplicatedReleasesAction) {
        return cashFlowService.delete(id, DuplicatedReleaseAction.valueOf(duplicatedReleasesAction));
    }
}
