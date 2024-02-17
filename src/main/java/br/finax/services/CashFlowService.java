package br.finax.services;

import br.finax.models.CashFlow;
import br.finax.models.DuplicatedReleaseBuilder;
import br.finax.models.User;
import br.finax.records.MontlhyCashFlow;
import br.finax.repository.CashFlowRepository;
import br.finax.utils.UtilsService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CashFlowService {

    private final CashFlowRepository cashFlowRepository;
    private final UtilsService utilsService;

    public CashFlow getById(Long id) {
        return cashFlowRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Release not found"));
    }

    public MontlhyCashFlow getMonthlyFlow(Date firstDt, Date lastDt, Date firstDtCurrentMonth) {
        User user = utilsService.getAuthUser();

        return new MontlhyCashFlow(
                cashFlowRepository.getMonthlyReleases(user.getId(), firstDt, lastDt),
                cashFlowRepository.getMonthlyBalance(user.getId(), firstDt, lastDt, firstDtCurrentMonth)
        );
    }

    public ResponseEntity<CashFlow> addRelease(CashFlow release, String releasedOn, int repeatFor) {
        try {
            if (release.getRepeat().isBlank()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(cashFlowRepository.save(release));
            }

            boolean isFixedRepeat = release.getRepeat().equals("fixed");
            Double installmentsAmount = release.getAmount() / repeatFor;

            if (!isFixedRepeat) {
                release.setAmount(installmentsAmount);
                release.setFixedBy("");
            }

            CashFlow savedRelease = cashFlowRepository.save(release);

            List<CashFlow> releases = new LinkedList<>();
            LocalDate dt = savedRelease.getDate();

            int loopQuantity = repeatFor - 1;

            for (var i = 0; i < loopQuantity; i++) {
                releases.add(
                        createDuplicatedRelease(
                                savedRelease,
                                isFixedRepeat ? savedRelease.getAmount() : installmentsAmount,
                                isFixedRepeat ? getNewDate(dt, release.getFixedBy()) : dt.plusMonths(1)
                        )
                );
                dt = releases.get(i).getDate();
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

    public ResponseEntity<CashFlow> editRelease(CashFlow release, String duplicatedReleaseAction) {
        try {
            CashFlow existingRelease = cashFlowRepository.findById(release.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

            release.setUserId(existingRelease.getUserId());
            release.setType(existingRelease.getType());
            release.setAttachment(existingRelease.getAttachment());
            release.setAttachmentName(existingRelease.getAttachmentName());
            release.setDuplicatedReleaseId(existingRelease.getDuplicatedReleaseId());
            release.setRepeat(existingRelease.getRepeat());
            release.setFixedBy(existingRelease.getFixedBy());

            boolean updatingAll = duplicatedReleaseAction.equals("all");

            if (!updatingAll) {
                cashFlowRepository.save(release);
            }

            if (duplicatedReleaseAction.equals("nexts") || updatingAll) {
                List<CashFlow> duplicatedReleases;

                if (duplicatedReleaseAction.equals("nexts")) {
                    duplicatedReleases = cashFlowRepository.getNextDuplicatedReleases(
                            release.getDuplicatedReleaseId() == null ? release.getId() : release.getDuplicatedReleaseId(),
                            existingRelease.getDate()
                    );
                } else {
                    duplicatedReleases = cashFlowRepository.getAllDuplicatedReleases(
                            release.getDuplicatedReleaseId() == null ? release.getId() : release.getDuplicatedReleaseId()
                    );
                }

                for (CashFlow item: duplicatedReleases) {
                    item.setDescription(release.getDescription());
                    item.setAccountId(release.getAccountId());
                    item.setAmount(release.getAmount());
                    item.setTargetAccountId(release.getTargetAccountId());
                    item.setCategoryId(release.getCategoryId());
                    item.setDate(item.getDate());
                    item.setTime(release.getTime());
                    item.setObservation(release.getObservation());
                    item.setDone(release.isDone());
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

    public ResponseEntity<CashFlow> addAttachment(Long id, MultipartFile attachment) {
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
                    release.setAttachment(utilsService.compressImage(attachment.getBytes(), true));
            }

            release.setAttachmentName(attachment.getOriginalFilename());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().body(cashFlowRepository.save(release));
    }

    public ResponseEntity<CashFlow> removeAttachment(Long id) {
        CashFlow release = cashFlowRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        release.setAttachment(null);
        release.setAttachmentName(null);

        return ResponseEntity.ok().body(cashFlowRepository.save(release));
    }

    public ResponseEntity<byte[]> getAttachment(Long id) {
        CashFlow release = cashFlowRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        return ResponseEntity.ok().body(release.getAttachment());
    }

    public ResponseEntity<?> delete(Long id, String duplicatedReleasesAction) {
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
