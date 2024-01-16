package br.finax.controllers;

import br.finax.enums.ImgFormat;
import br.finax.models.CashFlow;
import br.finax.models.InterfacesSQL;
import br.finax.repository.CashFlowRepository;
import br.finax.utils.UtilsService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private MonthlyFlow getMonthlyFlow(
            @RequestParam Long userId, @RequestParam Integer year, @RequestParam Integer month
    ) {
        return new MonthlyFlow(
            cashFlowRepository.getCashFlow(userId, year, month),
            cashFlowRepository.getMonthlyBalance(userId).get(0)
        );
    }

    @PostMapping
    private CashFlow save(@RequestBody Release cashFlow) {
        try {
            boolean isNewRelease = cashFlow.release.getId() == null;

            if (!isNewRelease) {
                CashFlow release = cashFlowRepository.findById(cashFlow.getRelease().getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

                cashFlow.release.setAttachment(release.getAttachment());
                cashFlow.release.setAttachmentName(release.getAttachmentName());
            }

            Double installmentsAmount = cashFlow.release.getAmount() / cashFlow.installmentsBy;
            if (cashFlow.repeat.equals("installments")) {
                cashFlow.release.setAmount(installmentsAmount);
            }

            CashFlow release = cashFlowRepository.save(cashFlow.release);

            if (!cashFlow.repeat.isBlank() && isNewRelease) {
                List<CashFlow> releases = new ArrayList<>();
                LocalDate dt = release.getDate();

                for (var i = 0; i < (cashFlow.repeat.equals("fixed") ? getLoopQuantity(cashFlow.fixedBy) : cashFlow.installmentsBy - 1); i++) {
                    CashFlow duplicatedRelease = new CashFlow();
                    duplicatedRelease.setDescription(release.getDescription());
                    duplicatedRelease.setAccountId(release.getAccountId());
                    duplicatedRelease.setType(release.getType());
                    duplicatedRelease.setDone(false);
                    duplicatedRelease.setTargetAccountId(release.getTargetAccountId());
                    duplicatedRelease.setCategoryId(release.getCategoryId());
                    duplicatedRelease.setTime(release.getTime());
                    duplicatedRelease.setObservation(release.getObservation());
                    duplicatedRelease.setDuplicatedReleaseId(release.getId());
                    duplicatedRelease.setUserId(release.getUserId());

                    if (cashFlow.repeat.equals("fixed")) {
                        duplicatedRelease.setAmount(release.getAmount());
                        duplicatedRelease.setDate(getNewDate(dt, cashFlow.fixedBy));
                    } else {
                        duplicatedRelease.setAmount(installmentsAmount);
                        duplicatedRelease.setDate(dt.plusMonths(1));
                    }

                    dt = duplicatedRelease.getDate();

                    releases.add(duplicatedRelease);
                }

                cashFlowRepository.saveAll(releases);
            }

            return release;
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

        String fileExtension = Objects.requireNonNull(attachment.getOriginalFilename()).split("\\.")[1];

        try {
            switch (fileExtension) {
                case "pdf":
                    release.setAttachment(utilsService.compressPdf(attachment.getBytes()));
                    break;
                case "png", "webp":
                    release.setAttachment(attachment.getBytes());
                    break;
                default:
                    release.setAttachment(utilsService.compressImage(attachment.getBytes(), true, ImgFormat.JPG));
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
    private ResponseEntity<?> delete(@PathVariable Long id, @RequestParam String duplicatedReleasesAction) {
        try {
            CashFlow release = cashFlowRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

            if (duplicatedReleasesAction.equals("nexts")) {
                List<CashFlow> nextDuplicatedReleases = cashFlowRepository.getNextDuplicatedReleases(
                        release.getDuplicatedReleaseId() != null ? release.getDuplicatedReleaseId() : id,
                        release.getDate()
                );
                cashFlowRepository.deleteAll(nextDuplicatedReleases);
            } else if (duplicatedReleasesAction.equals("all")) {
                List<CashFlow> allDuplicatedReleases = cashFlowRepository.getAllDuplicatedReleases(
                        release.getDuplicatedReleaseId() != null ? release.getDuplicatedReleaseId() : id
                );
                cashFlowRepository.deleteAll(allDuplicatedReleases);
            }

            if (!duplicatedReleasesAction.equals("all"))
                cashFlowRepository.deleteById(id);

            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Data
    private static class Release {
        String repeat;
        String fixedBy;
        int installmentsBy;
        CashFlow release;
    }

    @Data
    @AllArgsConstructor
    private static class MonthlyFlow {
        List<InterfacesSQL.MonthlyReleases> releases;
        InterfacesSQL.MonthlyBalance totals;
    }

    int getLoopQuantity(String fixedBy) {
        return switch (fixedBy) {
            case "daily" -> 365; // 1 year
            case "weekly" -> 52; // 1 year
            case "monthly" -> 18; // 1,5 year
            case "bimonthly" -> 12; // 2 years
            case "quarterly" -> 8; // 2 years
            case "biannual" -> 6; // 5 years
            case "annual" -> 10; // 10 years
            default -> 0;
        };
    }

    LocalDate getNewDate(LocalDate dt, String fixedBy) {
        return switch (fixedBy) {
            case "daily" -> dt.plusDays(1);
            case "weekly" -> dt.plusWeeks(1);
            case "monthly" -> dt.plusMonths(1);
            case "bimonthly" -> dt.plusMonths(2);
            case "quarterly" -> dt.plusMonths(3);
            case "biannual" -> dt.plusMonths(6);
            case "annual" -> dt.plusYears(1);
            default -> dt;
        };
    }
}
