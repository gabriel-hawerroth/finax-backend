package br.finax.services;

import br.finax.dto.CashFlowValues;
import br.finax.dto.DuplicatedReleaseBuilder;
import br.finax.dto.InterfacesSQL;
import br.finax.dto.MonthlyCashFlow;
import br.finax.enums.ErrorCategory;
import br.finax.enums.S3FolderPath;
import br.finax.enums.release.DuplicatedReleaseAction;
import br.finax.enums.release.ReleaseFixedby;
import br.finax.enums.release.ReleaseRepeat;
import br.finax.exceptions.*;
import br.finax.external.AwsS3Service;
import br.finax.models.Release;
import br.finax.repository.ReleaseRepository;
import br.finax.utils.FileUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;

import static br.finax.external.AwsS3Service.getS3FileName;
import static br.finax.utils.FileUtils.convertByteArrayToFile;
import static br.finax.utils.FileUtils.getFileExtension;
import static br.finax.utils.UtilsService.getAuthUser;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class ReleaseService {

    private final ReleaseRepository releaseRepository;
    private final CreditCardService creditCardService;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final AwsS3Service awsS3Service;

    @Transactional(readOnly = true)
    public Release findById(@NonNull Long id) {
        final Release release = releaseRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        checkPermission(release);

        return release;
    }

    @Transactional(readOnly = true)
    public MonthlyCashFlow getMonthlyFlow(
            final LocalDate firstDt, final LocalDate lastDt
    ) {
        if (ChronoUnit.DAYS.between(firstDt, lastDt) > 31)
            throw new InvalidParametersException("The difference between the firstDt and lastDt should not exceed 31 days");

        return new MonthlyCashFlow(
                releaseRepository.getMonthlyReleases(getAuthUser().getId(), firstDt, lastDt),
                0
        );
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
    public Release addRelease(final @NonNull Release release, final int repeatFor) {
        release.setId(null);
        release.setUserId(getAuthUser().getId());

        if (release.getRepeat() == null)
            return releaseRepository.save(release);

        final boolean isFixedRepeat = release.getRepeat().equals(ReleaseRepeat.FIXED);
        BigDecimal installmentsAmount = release.getAmount().divide(BigDecimal.valueOf(repeatFor), RoundingMode.HALF_EVEN);

        if (!isFixedRepeat) {
            release.setAmount(installmentsAmount);
            release.setFixedBy(null);
        }

        final Release savedRelease = releaseRepository.save(release);

        final List<Release> releases = new LinkedList<>();
        LocalDate dt = savedRelease.getDate();

        for (var i = 0; i < repeatFor - 1; i++) {
            final Release newRelease = createDuplicatedRelease(
                    savedRelease,
                    isFixedRepeat ? savedRelease.getAmount() : installmentsAmount,
                    isFixedRepeat ? getNewDate(dt, release.getFixedBy()) : dt.plusMonths(1)
            );

            releases.add(newRelease);
            dt = releases.get(i).getDate();
        }

        releaseRepository.saveAll(releases);

        return savedRelease;
    }

    @Transactional
    public Release editRelease(
            @NonNull Release release, @NonNull DuplicatedReleaseAction duplicatedReleaseAction
    ) {
        checkPermission(release);

        final boolean updatingAll = duplicatedReleaseAction == DuplicatedReleaseAction.ALL;
        final boolean updatingNexts = duplicatedReleaseAction == DuplicatedReleaseAction.NEXTS;

        final Release existingRelease = findById(release.getId());

        // things that can't change
        release.setUserId(existingRelease.getUserId());
        release.setType(existingRelease.getType());
        release.setAttachmentS3FileName(existingRelease.getAttachmentS3FileName());
        release.setAttachmentName(existingRelease.getAttachmentName());
        release.setDuplicatedReleaseId(existingRelease.getDuplicatedReleaseId());
        release.setRepeat(existingRelease.getRepeat());
        release.setFixedBy(existingRelease.getFixedBy());

        if (!updatingAll)
            release = releaseRepository.save(release);

        if (updatingNexts || updatingAll) {
            final List<Release> duplicatedReleases;

            if (updatingNexts) {
                duplicatedReleases = releaseRepository.getNextDuplicatedReleases(
                        release.getDuplicatedReleaseId() == null ? release.getId() : release.getDuplicatedReleaseId(),
                        existingRelease.getDate()
                );
            } else {
                duplicatedReleases = releaseRepository.getAllDuplicatedReleases(
                        release.getDuplicatedReleaseId() == null ? release.getId() : release.getDuplicatedReleaseId()
                );
            }

            for (Release item : duplicatedReleases) {
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

            releaseRepository.saveAll(duplicatedReleases);
        }

        return release;
    }

    @Transactional
    public Release addAttachment(long releaseId, final @NonNull MultipartFile attachment) {
        final Release release = findById(releaseId);

        checkPermission(release);

        final String fileExtension = getFileExtension(attachment);
        final String fileName = getS3FileName(releaseId, fileExtension, S3FolderPath.USER_ATTACHMENTS);

        try {
            final byte[] compressedFile = FileUtils.compressFile(attachment);

            final File tempFile = convertByteArrayToFile(compressedFile, fileName);

            try {
                if (release.getAttachmentS3FileName() != null)
                    awsS3Service.updateS3File(release.getAttachmentS3FileName(), fileName, tempFile);
                else
                    awsS3Service.uploadS3File(fileName, tempFile);
            } finally {
                var _ = tempFile.delete();
            }

            release.setAttachmentS3FileName(fileName);
            release.setAttachmentName(attachment.getOriginalFilename());

            return releaseRepository.save(release);
        } catch (FileCompressionErrorException | FileIOException | ServiceException e) {
            throw new ServiceException(ErrorCategory.INTERNAL_ERROR, "Failed to process the file", e);
        }
    }

    @Transactional
    public Release removeAttachment(long releaseId) {
        final Release release = findById(releaseId);

        checkPermission(release);

        awsS3Service.deleteS3File(release.getAttachmentS3FileName());

        release.setAttachmentS3FileName(null);
        release.setAttachmentName(null);

        return releaseRepository.save(release);
    }

    @Transactional(readOnly = true)
    public byte[] getAttachment(long releaseId) {
        final Release release = findById(releaseId);

        checkPermission(release);

        return awsS3Service.getS3File(release.getAttachmentS3FileName());
    }

    @Transactional
    public void delete(long releaseId, DuplicatedReleaseAction duplicatedReleasesAction) {
        final Release release = findById(releaseId);

        checkPermission(release);

        switch (duplicatedReleasesAction) {
            case NEXTS -> releaseRepository.deleteAll(
                    releaseRepository.getNextDuplicatedReleases(
                            release.getDuplicatedReleaseId() != null ? release.getDuplicatedReleaseId() : releaseId,
                            release.getDate()
                    )
            );
            case ALL -> releaseRepository.deleteAll(
                    releaseRepository.getAllDuplicatedReleases(
                            release.getDuplicatedReleaseId() != null ? release.getDuplicatedReleaseId() : releaseId
                    )
            );
            default -> { /* do nothing */ }
        }

        if (duplicatedReleasesAction != DuplicatedReleaseAction.ALL)
            releaseRepository.deleteById(releaseId);
    }

    private Release createDuplicatedRelease(final Release original, BigDecimal newAmount, final LocalDate newDate) {
        return new DuplicatedReleaseBuilder(original)
                .amount(newAmount)
                .date(newDate)
                .build();
    }

    private LocalDate getNewDate(final LocalDate dt, final ReleaseFixedby fixedBy) {
        return switch (fixedBy) {
            case DAILY -> dt.plusDays(1);
            case WEEKLY -> dt.plusWeeks(1);
            case MONTHLY -> dt.plusMonths(1);
            case BIMONTHLY -> dt.plusMonths(2);
            case QUARTERLY -> dt.plusMonths(3);
            case BIANNUAL -> dt.plusMonths(6);
            case ANNUAL -> dt.plusYears(1);
        };
    }

    @Transactional(readOnly = true)
    public InterfacesSQL.HomeRevenueExpense getHomeBalances(long userId, LocalDate firstDt, LocalDate lastDt) {
        return releaseRepository.getHomeBalances(userId, firstDt, lastDt);
    }

    @Transactional(readOnly = true)
    public List<InterfacesSQL.HomeUpcomingRelease> getPayableAndReceivableAccounts(long userId, LocalDate firstDt, LocalDate lastDt) {
        return releaseRepository.getPayableAndReceivableAccounts(userId, firstDt, lastDt);
    }

    @Transactional(readOnly = true)
    public List<Release> findReleasesForHomeSpendsCategory(long id, LocalDate startDate, LocalDate endDate) {
        return releaseRepository.findReleasesForHomeSpendsCategory(id, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<InterfacesSQL.MonthlyRelease> getByInvoice(long userId, long creditCardId, LocalDate firstDt, LocalDate lastDt) {
        return releaseRepository.getByInvoice(userId, creditCardId, firstDt, lastDt);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCurrentCardInvoiceAmount(long cardId, LocalDate startDt, LocalDate endDt) {
        return releaseRepository.getCurrentCardInvoiceAmount(cardId, startDt, endDt);
    }

    private void checkPermission(final Release release) {
        if (release.getUserId() != getAuthUser().getId())
            throw new WithoutPermissionException();
    }
}
