package br.finax.controllers;

import br.finax.models.CashFlow;
import br.finax.models.InterfacesSQL;
import br.finax.repository.CashFlowRepository;
import br.finax.utils.UtilsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/cash-flow")
public class CashFlowController {

    @Autowired
    private CashFlowRepository cashFlowRepository;
    @Autowired
    private UtilsService utilsService;

    @GetMapping("/{id}")
    private CashFlow getById(@PathVariable Long id) {
        return cashFlowRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lançamento não encontrado")
        );
    }

    @GetMapping
    private List<InterfacesSQL.MonthlyCashFlow> getMonthlyReleases(
            @RequestParam Long userId, @RequestParam Integer year, @RequestParam Integer month
    ) {
        return cashFlowRepository.getCashFlow(userId, year, month);
    }

    @PostMapping
    private CashFlow save(@RequestBody CashFlow cashFlow) {
        try {
            if (cashFlow.getId() != null) {
                CashFlow release = cashFlowRepository.findById(cashFlow.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

                cashFlow.setAttachment(release.getAttachment());
                cashFlow.setAttachmentName(release.getAttachmentName());
            }

            return cashFlowRepository.save(cashFlow);
        } catch (RuntimeException e) {
            if (e.getCause().getCause().toString().contains("Insufficient balance for this transaction")) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Insufficient balance for this transaction");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/add-attachment/{release_id}")
    private ResponseEntity<CashFlow> addAttachment(@PathVariable("release_id") Long id,
                            @RequestParam("file") MultipartFile attachment) throws IOException {
        CashFlow release = cashFlowRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (attachment.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        try {
            if (Objects.requireNonNull(attachment.getOriginalFilename()).split("\\.")[1].equals("pdf")) {
                release.setAttachment(utilsService.compressPdf(attachment.getBytes()));
            } else {
                release.setAttachment(utilsService.compressImage(attachment.getBytes(), attachment.getSize(), true));
            }

            release.setAttachmentName(attachment.getOriginalFilename());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().body(cashFlowRepository.save(release));
    }

    @PutMapping("/remove-attachment/{release_id}")
    private ResponseEntity<CashFlow> removeAttachment(@PathVariable("release_id") Long id) {
        CashFlow release = cashFlowRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        release.setAttachment(null);
        release.setAttachmentName(null);

        return ResponseEntity.ok().body(cashFlowRepository.save(release));
    }

    @Cacheable
    @GetMapping("/get-attachment/{release_id}")
    private ResponseEntity<byte[]> getAttachment(@PathVariable("release_id") Long id) {
        CashFlow release = cashFlowRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        return ResponseEntity.ok().body(release.getAttachment());
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            cashFlowRepository.deleteById(id);

            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
