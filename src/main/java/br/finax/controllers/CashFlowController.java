package br.finax.controllers;

import br.finax.enums.ImgFormat;
import br.finax.models.CashFlow;
import br.finax.models.DuplicatedReleaseBuilder;
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
import java.time.temporal.ChronoUnit;
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
    private ResponseEntity<CashFlow> addRelease(@RequestBody CashFlow release, @RequestParam int installmentsBy) {
        try {
            if (release.getRepeat().isBlank()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(cashFlowRepository.save(release));
            }

            Double installmentsAmount = release.getAmount() / installmentsBy;
            if (release.getRepeat().equals("installments")) {
                release.setAmount(installmentsAmount);
            }

            boolean isFixedRepeat = release.getRepeat().equals("fixed");

            if (!isFixedRepeat)
                release.setFixedBy("");

            CashFlow savedRelease = cashFlowRepository.save(release);

            List<CashFlow> releases = new ArrayList<>();
            LocalDate dt = savedRelease.getDate();

            int loopQuantity = isFixedRepeat ? getLoopQuantity(release.getFixedBy()) : installmentsBy - 1;

            for (var i = 0; i < loopQuantity; i++) {
                CashFlow duplicatedRelease = createDuplicatedRelease(
                        savedRelease,
                        isFixedRepeat ? savedRelease.getAmount() : installmentsAmount,
                        isFixedRepeat ? getNewDate(dt, release.getFixedBy()) : dt.plusMonths(1)
                );

                releases.add(duplicatedRelease);

                dt = duplicatedRelease.getDate();
            }

            cashFlowRepository.saveAll(releases);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedRelease);
        } catch (RuntimeException e) {
            if (e.getCause().getCause().toString().contains("Insufficient balance for this transaction")) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Insufficient balance for this transaction");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping
    private ResponseEntity<CashFlow> editRelease(@RequestBody CashFlow release, @RequestParam String duplicatedReleaseAction) {
        try {
            CashFlow existingRelease = cashFlowRepository.findById(release.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

            release.setUserId(existingRelease.getUserId());
            release.setType(existingRelease.getType());
            release.setAttachment(existingRelease.getAttachment());
            release.setAttachmentName(existingRelease.getAttachmentName());
            release.setDuplicatedReleaseId(existingRelease.getDuplicatedReleaseId());

            boolean updatingAll = duplicatedReleaseAction.equals("all");

            if (!duplicatedReleaseAction.equals("all")) {
                CashFlow savedRelease = cashFlowRepository.save(release);
            }

            if (duplicatedReleaseAction.equals("nexts") || updatingAll) {
                List<CashFlow> duplicatedReleases;

                if (duplicatedReleaseAction.equals("nexts")) {
                    duplicatedReleases = cashFlowRepository.getNextDuplicatedReleases(
                            release.getDuplicatedReleaseId() == null ? release.getId() : release.getDuplicatedReleaseId(),
                            release.getDate()
                    );
                } else {
                    duplicatedReleases = cashFlowRepository.getAllDuplicatedReleases(
                            release.getDuplicatedReleaseId() == null ? release.getId() : release.getDuplicatedReleaseId()
                    );
                }

                LocalDate dt = release.getDate();

                boolean firstLoop = true;

                for (CashFlow item: duplicatedReleases) {
                    item.setDescription(release.getDescription());
                    item.setAccountId(release.getAccountId());
                    item.setAmount(release.getAmount());
                    item.setTargetAccountId(release.getTargetAccountId());
                    item.setCategoryId(release.getCategoryId());
                    item.setDate(release.getRepeat().equals("fixed") ? getNewDate(dt, release.getFixedBy()) :
                            (firstLoop && updatingAll ? dt : dt.plusMonths(1)));
                    item.setTime(release.getTime());
                    item.setObservation(release.getObservation());

                    if (firstLoop && updatingAll) {
                        item.setDone(release.isDone());
                    }

                    dt = item.getDate();

                    firstLoop = false;
                }

                cashFlowRepository.saveAll(duplicatedReleases);
            }

            return ResponseEntity.ok().body(release);
        } catch (RuntimeException e) {
            if (e.getCause().getCause().toString().contains("Insufficient balance for this transaction")) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Insufficient balance for this transaction");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/add-attachment/{release_id}")
    private ResponseEntity<CashFlow> addAttachment(@PathVariable("release_id") Long id,
                            @RequestParam("file") MultipartFile attachment) {
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

    private CashFlow createDuplicatedRelease(CashFlow original, Double newAmount, LocalDate newDate) {
        return new DuplicatedReleaseBuilder(original)
                .amount(newAmount)
                .date(newDate)
                .build();
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

    @Data
    @AllArgsConstructor
    private static class MonthlyFlow {
        List<InterfacesSQL.MonthlyReleases> releases;
        InterfacesSQL.MonthlyBalance totals;
    }
}
