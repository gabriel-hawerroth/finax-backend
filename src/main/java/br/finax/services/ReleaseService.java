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
import br.finax.exceptions.InvalidParametersException;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.ServiceException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.Release;
import br.finax.repository.ReleaseRepository;
import br.finax.utils.FileUtils;
import br.finax.utils.UtilsService;
import lombok.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static br.finax.utils.FileUtils.convertByteArrayToFile;
import static br.finax.utils.FileUtils.getFileExtension;

@Service
public class ReleaseService {

    private final ReleaseRepository releaseRepository;

    private final CreditCardService creditCardService;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final AwsS3Service awsS3Service;

    private final UtilsService utils;
    private final FileUtils fileUtils;

    @Lazy
    public ReleaseService(ReleaseRepository releaseRepository, CreditCardService creditCardService, AccountService accountService, CategoryService categoryService, AwsS3Service awsS3Service, UtilsService utils, FileUtils fileUtils) {
        this.releaseRepository = releaseRepository;
        this.creditCardService = creditCardService;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.awsS3Service = awsS3Service;
        this.utils = utils;
        this.fileUtils = fileUtils;
    }

    @Transactional(readOnly = true)
    public Release findById(@NonNull Long id) {
        return releaseRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional(readOnly = true)
    public MonthlyCashFlow getMonthlyFlow(
            final LocalDate firstDt, final LocalDate lastDt
    ) {
        final long userId = utils.getAuthUser().getId();

        if (ChronoUnit.DAYS.between(firstDt, lastDt) > 31)
            throw new InvalidParametersException("The difference between the firstDt and lastDt should not exceed 31 days");

        return new MonthlyCashFlow(
                releaseRepository.getMonthlyReleases(userId, firstDt, lastDt), 0
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
        release.setUserId(utils.getAuthUser().getId());

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
        final String fileName = awsS3Service.getS3FileName(releaseId, fileExtension, S3FolderPath.USER_ATTACHMENTS);

        try (final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            final Future<byte[]> compressedFileFuture = executor.submit(() -> fileUtils.compressFile(attachment));
            final byte[] compressedFile = compressedFileFuture.get();

            final Future<File> tempFileFuture = executor.submit(() -> convertByteArrayToFile(compressedFile, fileName));
            final File tempFile = tempFileFuture.get();

            if (release.getAttachmentS3FileName() != null)
                executor.submit(() -> awsS3Service.updateS3File(release.getAttachmentS3FileName(), fileName, tempFile)).get();
            else
                executor.submit(() -> awsS3Service.uploadS3File(fileName, tempFile)).get();

            var _ = tempFile.delete();

            release.setAttachmentS3FileName(fileName);
            release.setAttachmentName(attachment.getOriginalFilename());

            return releaseRepository.save(release);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException(ErrorCategory.INTERNAL_ERROR, e.getMessage(), e);
        }
    }

    @Transactional
    public Release removeAttachment(long releaseId) {
        final Release release = findById(releaseId);

        checkPermission(release);

        try (final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> awsS3Service.deleteS3File(release.getAttachmentS3FileName()));
        }

        release.setAttachmentS3FileName(null);
        release.setAttachmentName(null);

        return releaseRepository.save(release);
    }

    @Transactional(readOnly = true)
    public byte[] getAttachment(long releaseId) {
        final Release release = findById(releaseId);

        checkPermission(release);

        try (final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            final Future<byte[]> fileFuture = executor.submit(() -> awsS3Service.getS3File(release.getAttachmentS3FileName()));
            return fileFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException(ErrorCategory.INTERNAL_ERROR, e.getMessage(), e);
        }
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
    public InterfacesSQL.HomeBalances getHomeBalances(long userId, Date firstDt, Date lastDt) {
        return releaseRepository.getHomeBalances(userId, firstDt, lastDt);
    }

    @Transactional(readOnly = true)
    public List<InterfacesSQL.MonthlyReleases> getUpcomingReleasesExpected(long userId) {
        return releaseRepository.getUpcomingReleasesExpected(userId);
    }

    @Transactional(readOnly = true)
    public List<Release> findReleasesForHomeSpendsCategory(long id, LocalDate startDate, LocalDate endDate) {
        return releaseRepository.findReleasesForHomeSpendsCategory(id, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<InterfacesSQL.MonthlyReleases> getByInvoice(long userId, long creditCardId, Date firstDt, Date lastDt) {
        return releaseRepository.getByInvoice(userId, creditCardId, firstDt, lastDt);
    }

    private void checkPermission(final Release release) {
        if (release.getUserId() != utils.getAuthUser().getId())
            throw new WithoutPermissionException();
    }
}
