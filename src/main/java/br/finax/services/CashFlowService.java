package br.finax.services;

import br.finax.dto.CashFlowValues;
import br.finax.dto.MonthlyCashFlow;
import br.finax.enums.DuplicatedReleaseAction;
import br.finax.enums.ReleasesViewMode;
import br.finax.exceptions.CompressionErrorException;
import br.finax.exceptions.EmptyFileException;
import br.finax.exceptions.NotFoundException;
import br.finax.models.CashFlow;
import br.finax.models.DuplicatedReleaseBuilder;
import br.finax.repository.AccountsRepository;
import br.finax.repository.CashFlowRepository;
import br.finax.repository.CategoryRepository;
import br.finax.repository.CreditCardRepository;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CashFlowService {

    private final CashFlowRepository cashFlowRepository;
    private final CreditCardRepository creditCardRepository;
    private final AccountsRepository accountsRepository;
    private final CategoryRepository categoryRepository;

    private final UtilsService utils;

    public MonthlyCashFlow getMonthlyFlow(
            final Date firstDt, final Date lastDt,
            final ReleasesViewMode viewMode, final Date firstDtCurrentMonth,
            final Date firstDtInvoice, final Date lastDtInvoice
    ) {
        final long userId = utils.getAuthUser().getId();

        return switch (viewMode) {
            case releases -> new MonthlyCashFlow(
                    cashFlowRepository.getMonthlyReleases(userId, firstDt, lastDt), 0
            );
            case invoice -> new MonthlyCashFlow(
                    cashFlowRepository.getMonthlyReleasesInvoiceMode(
                            userId, firstDt, lastDt, firstDtInvoice, lastDtInvoice
                    ),
                    cashFlowRepository.getExpectedBalance(userId, firstDtCurrentMonth, lastDt)
            );
        };
    }

    public CashFlowValues getValues() {
        final long userId = utils.getAuthUser().getId();

        return new CashFlowValues(
                accountsRepository.getBasicList(userId),
                categoryRepository.findByUser(userId),
                creditCardRepository.getBasicList(userId)
        );
    }

    public CashFlow addRelease(final CashFlow release, final int repeatFor) {
        if (release.getRepeat().isBlank())
            return cashFlowRepository.save(release);

        final boolean isFixedRepeat = release.getRepeat().equals("fixed");
        BigDecimal installmentsAmount = release.getAmount().divide(BigDecimal.valueOf(repeatFor), RoundingMode.HALF_EVEN);

        if (!isFixedRepeat) {
            release.setAmount(installmentsAmount);
            release.setFixedBy("");
        }

        final CashFlow savedRelease = cashFlowRepository.save(release);

        final List<CashFlow> releases = new LinkedList<>();
        LocalDate dt = savedRelease.getDate();

        for (var i = 0; i < repeatFor - 1; i++) {
            final CashFlow newRelease = createDuplicatedRelease(
                    savedRelease,
                    isFixedRepeat ? savedRelease.getAmount() : installmentsAmount,
                    isFixedRepeat ? getNewDate(dt, release.getFixedBy()) : dt.plusMonths(1)
            );

            releases.add(newRelease);
            dt = releases.get(i).getDate();
        }

        cashFlowRepository.saveAll(releases);

        return savedRelease;
    }

    public CashFlow editRelease(
            CashFlow release, final DuplicatedReleaseAction duplicatedReleaseAction
    ) {
        final boolean updatingAll = duplicatedReleaseAction == DuplicatedReleaseAction.ALL;
        final boolean updatingNexts = duplicatedReleaseAction == DuplicatedReleaseAction.NEXTS;

        final CashFlow existingRelease = cashFlowRepository.findById(release.getId())
                .orElseThrow(NotFoundException::new);

        // things that can't change
        release.setUserId(existingRelease.getUserId());
        release.setType(existingRelease.getType());
        release.setAttachment(existingRelease.getAttachment());
        release.setAttachmentName(existingRelease.getAttachmentName());
        release.setDuplicatedReleaseId(existingRelease.getDuplicatedReleaseId());
        release.setRepeat(existingRelease.getRepeat());
        release.setFixedBy(existingRelease.getFixedBy());

        if (!updatingAll) {
            release = cashFlowRepository.save(release);
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

            for (CashFlow item : duplicatedReleases) {
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

        return release;
    }

    public CashFlow addAttachment(long id, final MultipartFile attachment) {
        final CashFlow release = cashFlowRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        if (attachment.isEmpty())
            throw new EmptyFileException();

        final String fileExtension = Objects.requireNonNull(attachment.getOriginalFilename()).split("\\.")[1];

        try {
            switch (fileExtension) {
                case "pdf":
                    release.setAttachment(utils.compressPdf(attachment.getBytes()));
                    break;
                case "png", "webp":
                    release.setAttachment(attachment.getBytes());
                    break;
                default:
                    release.setAttachment(utils.compressImage(attachment.getBytes(), true));
            }
        } catch (IOException e) {
            throw new CompressionErrorException();
        }

        release.setAttachmentName(attachment.getOriginalFilename());

        return cashFlowRepository.save(release);
    }

    public CashFlow removeAttachment(long id) {
        final CashFlow release = cashFlowRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        release.setAttachment(null);
        release.setAttachmentName(null);

        return cashFlowRepository.save(release);
    }

    public byte[] getAttachment(long id) {
        return cashFlowRepository.findById(id)
                .orElseThrow(NotFoundException::new)
                .getAttachment();
    }

    public void delete(long id, DuplicatedReleaseAction duplicatedReleasesAction) {
        final CashFlow release = cashFlowRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        switch (duplicatedReleasesAction) {
            case NEXTS -> cashFlowRepository.deleteAll(
                    cashFlowRepository.getNextDuplicatedReleases(
                            release.getDuplicatedReleaseId() != null ? release.getDuplicatedReleaseId() : id,
                            release.getDate()
                    )
            );
            case ALL -> cashFlowRepository.deleteAll(
                    cashFlowRepository.getAllDuplicatedReleases(
                            release.getDuplicatedReleaseId() != null ? release.getDuplicatedReleaseId() : id
                    )
            );
        }

        if (duplicatedReleasesAction != DuplicatedReleaseAction.ALL)
            cashFlowRepository.deleteById(id);
    }

    private CashFlow createDuplicatedRelease(final CashFlow original, BigDecimal newAmount, final LocalDate newDate) {
        return new DuplicatedReleaseBuilder(original)
                .amount(newAmount)
                .date(newDate)
                .build();
    }

    private LocalDate getNewDate(final LocalDate dt, final String fixedBy) {
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
