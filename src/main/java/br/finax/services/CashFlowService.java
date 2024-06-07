package br.finax.services;

import br.finax.dto.CashFlowValues;
import br.finax.dto.InterfacesSQL;
import br.finax.dto.MonthlyCashFlow;
import br.finax.enums.DuplicatedReleaseAction;
import br.finax.enums.ReleasesViewMode;
import br.finax.exceptions.InvalidParametersException;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.CashFlow;
import br.finax.models.DuplicatedReleaseBuilder;
import br.finax.repository.CashFlowRepository;
import br.finax.utils.FileUtils;
import br.finax.utils.UtilsService;
import lombok.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Service
public class CashFlowService {

    private final CashFlowRepository cashFlowRepository;

    private final CreditCardService creditCardService;
    private final CategoryService categoryService;
    private final AccountService accountService;

    private final UtilsService utils;
    private final FileUtils fileUtils;

    @Lazy
    public CashFlowService(CashFlowRepository cashFlowRepository, CreditCardService creditCardService, AccountService accountService, CategoryService categoryService, UtilsService utils, FileUtils fileUtils) {
        this.cashFlowRepository = cashFlowRepository;
        this.creditCardService = creditCardService;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.utils = utils;
        this.fileUtils = fileUtils;
    }

    @Transactional(readOnly = true)
    public CashFlow findById(@NonNull Long id) {
        return cashFlowRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional(readOnly = true)
    public MonthlyCashFlow getMonthlyFlow(
            final LocalDate firstDt, final LocalDate lastDt,
            final ReleasesViewMode viewMode, final Date firstDtCurrentMonth,
            final Date firstDtInvoice, final Date lastDtInvoice
    ) {
        final long userId = utils.getAuthUser().getId();

        if (ChronoUnit.DAYS.between(firstDt, lastDt) > 31)
            throw new InvalidParametersException("The difference between the firstDt and lastDt should not exceed 31 days");

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

    @Transactional(readOnly = true)
    public CashFlowValues getValues() {
        return new CashFlowValues(
                accountService.getBasicList(),
                categoryService.getByUser(),
                creditCardService.getBasicList()
        );
    }

    @Transactional
    public CashFlow addRelease(final @NonNull CashFlow release, final int repeatFor) {
        release.setUserId(utils.getAuthUser().getId());

        if (release.getRepeat() != null && release.getRepeat().isBlank())
            return cashFlowRepository.save(release);

        final boolean isFixedRepeat = release.getRepeat() != null && release.getRepeat().equals("fixed");
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

    @Transactional
    public CashFlow editRelease(
            @NonNull CashFlow release, @NonNull DuplicatedReleaseAction duplicatedReleaseAction
    ) {
        final boolean updatingAll = duplicatedReleaseAction == DuplicatedReleaseAction.ALL;
        final boolean updatingNexts = duplicatedReleaseAction == DuplicatedReleaseAction.NEXTS;

        final CashFlow existingRelease = findById(release.getId());

        // things that can't change
        release.setUserId(existingRelease.getUserId());
        release.setType(existingRelease.getType());
        release.setAttachment(existingRelease.getAttachment());
        release.setAttachmentName(existingRelease.getAttachmentName());
        release.setDuplicatedReleaseId(existingRelease.getDuplicatedReleaseId());
        release.setRepeat(existingRelease.getRepeat());
        release.setFixedBy(existingRelease.getFixedBy());

        if (!updatingAll)
            release = cashFlowRepository.save(release);

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

    @Transactional
    public CashFlow addAttachment(long releaseId, final @NonNull MultipartFile attachment) {
        final CashFlow release = findById(releaseId);

        checkPermission(release);

        release.setAttachment(fileUtils.compressFile(attachment, true));
        release.setAttachmentName(attachment.getOriginalFilename());

        return cashFlowRepository.save(release);
    }

    @Transactional
    public CashFlow removeAttachment(long releaseId) {
        final CashFlow release = findById(releaseId);

        checkPermission(release);

        release.setAttachment(null);
        release.setAttachmentName(null);

        return cashFlowRepository.save(release);
    }

    @Transactional(readOnly = true)
    public byte[] getAttachment(long releaseId) {
        final CashFlow release = findById(releaseId);

        checkPermission(release);

        return release.getAttachment();
    }

    @Transactional
    public void delete(long releaseId, DuplicatedReleaseAction duplicatedReleasesAction) {
        final CashFlow release = findById(releaseId);

        checkPermission(release);

        switch (duplicatedReleasesAction) {
            case NEXTS -> cashFlowRepository.deleteAll(
                    cashFlowRepository.getNextDuplicatedReleases(
                            release.getDuplicatedReleaseId() != null ? release.getDuplicatedReleaseId() : releaseId,
                            release.getDate()
                    )
            );
            case ALL -> cashFlowRepository.deleteAll(
                    cashFlowRepository.getAllDuplicatedReleases(
                            release.getDuplicatedReleaseId() != null ? release.getDuplicatedReleaseId() : releaseId
                    )
            );
        }

        if (duplicatedReleasesAction != DuplicatedReleaseAction.ALL)
            cashFlowRepository.deleteById(releaseId);
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

    @Transactional(readOnly = true)
    public InterfacesSQL.HomeBalances getHomeBalances(long userId, Date firstDt, Date lastDt) {
        return cashFlowRepository.getHomeBalances(userId, firstDt, lastDt);
    }

    @Transactional(readOnly = true)
    public List<InterfacesSQL.MonthlyReleases> getUpcomingReleasesExpected(long userId) {
        return cashFlowRepository.getUpcomingReleasesExpected(userId);
    }

    @Transactional(readOnly = true)
    public List<CashFlow> findReleasesForHomeSpendsCategory(long id, LocalDate startDate, LocalDate endDate) {
        return cashFlowRepository.findReleasesForHomeSpendsCategory(id, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<InterfacesSQL.MonthlyReleases> getByInvoice(long userId, long creditCardId, Date firstDt, Date lastDt) {
        return cashFlowRepository.getByInvoice(userId, creditCardId, firstDt, lastDt);
    }

    private void checkPermission(final CashFlow release) {
        if (release.getUserId() != utils.getAuthUser().getId())
            throw new WithoutPermissionException();
    }
}
