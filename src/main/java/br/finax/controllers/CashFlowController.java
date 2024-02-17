package br.finax.controllers;

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

    @GetMapping("/{id}")
    private CashFlow getById(@PathVariable Long id) {
        return cashFlowService.getById(id);
    }

    @GetMapping
    private MontlhyCashFlow getMonthlyFlow(@RequestParam Date firstDt, @RequestParam Date lastDt, @RequestParam Date firstDtCurrentMonth) {
        return cashFlowService.getMonthlyFlow(firstDt, lastDt, firstDtCurrentMonth);
    }

    @PostMapping
    private ResponseEntity<CashFlow> addRelease(@RequestBody CashFlow release, @RequestParam String releasedOn, @RequestParam int repeatFor) {
        return cashFlowService.addRelease(release, releasedOn, repeatFor);
    }

    @PutMapping
    private ResponseEntity<CashFlow> editRelease(@RequestBody CashFlow release, @RequestParam String duplicatedReleaseAction) {
        return cashFlowService.editRelease(release, duplicatedReleaseAction);
    }

    @PutMapping("/add-attachment/{id}")
    private ResponseEntity<CashFlow> addAttachment(@PathVariable Long id, @RequestParam MultipartFile file) {
        return cashFlowService.addAttachment(id, file);
    }

    @PutMapping("/remove-attachment/{id}")
    private ResponseEntity<CashFlow> removeAttachment(@PathVariable Long id) {
        return cashFlowService.removeAttachment(id);
    }

    @Cacheable
    @GetMapping("/get-attachment/{id}")
    private ResponseEntity<byte[]> getAttachment(@PathVariable Long id) {
        return cashFlowService.getAttachment(id);
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<?> delete(@PathVariable Long id, @RequestParam String duplicatedReleasesAction) {
        return cashFlowService.delete(id, duplicatedReleasesAction);
    }
}
