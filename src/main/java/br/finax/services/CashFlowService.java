package br.finax.services;

import br.finax.enums.DuplicatedReleaseAction;
import br.finax.enums.ReleasedOn;
import br.finax.models.CashFlow;
import br.finax.models.DuplicatedReleaseBuilder;
import br.finax.models.Invoice;
import br.finax.models.User;
import br.finax.records.MontlhyCashFlow;
import br.finax.repository.CashFlowRepository;
import br.finax.repository.InvoiceRepository;
import br.finax.utils.UtilsService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static br.finax.utils.UtilsService.compressImage;
import static br.finax.utils.UtilsService.compressPdf;

@Service
@RequiredArgsConstructor
public class CashFlowService {

    private final CashFlowRepository cashFlowRepository;
    private final InvoiceRepository invoiceRepository;
    private final UtilsService utilsService;

    public MontlhyCashFlow getMonthlyFlow(final Date firstDt, final Date lastDt, final Date firstDtCurrentMonth) {
        final User user = utilsService.getAuthUser();

        return new MontlhyCashFlow(
                cashFlowRepository.getMonthlyReleases(user.getId(), firstDt, lastDt),
                cashFlowRepository.getMonthlyBalance(user.getId(), firstDt, lastDt, firstDtCurrentMonth)
        );
    }

    public ResponseEntity<CashFlow> addRelease(final CashFlow release, final ReleasedOn releasedOn, final int repeatFor) {
        try {
            if (releasedOn.equals(ReleasedOn.CREDIT_CARD)) {
                checkInvoice(release);
            }

            if (release.getRepeat().isBlank()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(cashFlowRepository.save(release));
            }

            final boolean isFixedRepeat = release.getRepeat().equals("fixed");
            double installmentsAmount = release.getAmount() / repeatFor;

            if (!isFixedRepeat) {
                release.setAmount(installmentsAmount);
                release.setFixedBy("");
            }

            final CashFlow savedRelease = cashFlowRepository.save(release);

            final List<CashFlow> releases = new LinkedList<>();
            LocalDate dt = savedRelease.getDate();

            for (var i = 0; i < repeatFor - 1; i++) {
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

    public ResponseEntity<CashFlow> editRelease(
            final CashFlow release, final ReleasedOn releasedOn, final DuplicatedReleaseAction duplicatedReleaseAction
    ) {
        try {
            final boolean updatingAll = duplicatedReleaseAction == DuplicatedReleaseAction.ALL;
            final boolean updatingNexts = duplicatedReleaseAction == DuplicatedReleaseAction.NEXTS;

            if (releasedOn.equals(ReleasedOn.CREDIT_CARD)) {
                checkInvoice(release);
            }

            final CashFlow existingRelease = cashFlowRepository.findById(release.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

            // things that can't change
            release.setUserId(existingRelease.getUserId());
            release.setType(existingRelease.getType());
            release.setAttachment(existingRelease.getAttachment());
            release.setAttachmentName(existingRelease.getAttachmentName());
            release.setDuplicatedReleaseId(existingRelease.getDuplicatedReleaseId());
            release.setRepeat(existingRelease.getRepeat());
            release.setFixedBy(existingRelease.getFixedBy());

            if (!updatingAll) {
                cashFlowRepository.save(release);
            }

            if (updatingNexts || updatingAll) {
                final List<CashFlow> duplicatedReleases;

                if (updatingNexts) {
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

    public ResponseEntity<CashFlow> addAttachment(long id, final MultipartFile attachment) {
        final CashFlow release = cashFlowRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (attachment.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        final String fileExtension = Objects.requireNonNull(attachment.getOriginalFilename()).split("\\.")[1];

        try {
            switch (fileExtension) {
                case "pdf":
                    release.setAttachment(compressPdf(attachment.getBytes()));
                    break;
                case "png", "webp":
                    release.setAttachment(attachment.getBytes());
                    break;
                default:
                    release.setAttachment(compressImage(attachment.getBytes(), true));
            }

            release.setAttachmentName(attachment.getOriginalFilename());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().body(cashFlowRepository.save(release));
    }

    public ResponseEntity<CashFlow> removeAttachment(long id) {
        final CashFlow release = cashFlowRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        release.setAttachment(null);
        release.setAttachmentName(null);

        return ResponseEntity.ok().body(cashFlowRepository.save(release));
    }

    public ResponseEntity<byte[]> getAttachment(long id) {
        return ResponseEntity.ok().body(
                cashFlowRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST))
                        .getAttachment()
        );
    }

    public ResponseEntity<?> delete(long id, DuplicatedReleaseAction duplicatedReleasesAction) {
        try {
            final CashFlow release = cashFlowRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

            if (duplicatedReleasesAction == DuplicatedReleaseAction.NEXTS) {
                cashFlowRepository.deleteAll(
                        cashFlowRepository.getNextDuplicatedReleases(
                                release.getDuplicatedReleaseId() != null ? release.getDuplicatedReleaseId() : id,
                                release.getDate()
                        )
                );
            } else if (duplicatedReleasesAction == DuplicatedReleaseAction.ALL) {
                cashFlowRepository.deleteAll(
                        cashFlowRepository.getAllDuplicatedReleases(
                                release.getDuplicatedReleaseId() != null ? release.getDuplicatedReleaseId() : id
                        )
                );
            }

            if (duplicatedReleasesAction != DuplicatedReleaseAction.ALL)
                cashFlowRepository.deleteById(id);

            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private CashFlow createDuplicatedRelease(final CashFlow original, double newAmount, final LocalDate newDate) {
        return new DuplicatedReleaseBuilder(original)
                .amount(newAmount)
                .date(newDate)
                .build();
    }

    final LocalDate getNewDate(final LocalDate dt, final String fixedBy) {
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

    final void checkInvoice(final CashFlow release) {
        Long invoiceId = invoiceRepository.checkIfExists(
                release.getUserId(), release.getAccountId(), formatDt(release.getDate())
        );

        if (invoiceId == null) {
            invoiceId = invoiceRepository.save(
                    new Invoice(
                            release.getUserId(), release.getAccountId(), formatDt(release.getDate())
                    )
            ).getId();
        }

        release.setInvoice_id(invoiceId);
        release.setAccountId(null);
    }

    final String formatDt(final LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MM/yyyy"));
    }
}
