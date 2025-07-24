package br.finax.services;

import br.finax.dto.FirstAndLastDate;
import br.finax.dto.InterfacesSQL.HomeRevenueExpense;
import br.finax.dto.InterfacesSQL.HomeUpcomingRelease;
import br.finax.dto.cash_flow.CashFlowValues;
import br.finax.dto.cash_flow.DuplicatedReleaseBuilder;
import br.finax.dto.cash_flow.MonthlyRelease;
import br.finax.dto.cash_flow.MonthlyReleaseAccount;
import br.finax.dto.cash_flow.MonthlyReleaseCard;
import br.finax.dto.cash_flow.MonthlyReleaseCategory;
import br.finax.enums.ErrorCategory;
import br.finax.enums.S3FolderPath;
import br.finax.enums.release.DuplicatedReleaseAction;
import br.finax.enums.release.ReleaseFixedby;
import br.finax.enums.release.ReleaseRepeat;
import br.finax.enums.release.ReleaseType;
import br.finax.exceptions.FileCompressionErrorException;
import br.finax.exceptions.FileIOException;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.ServiceException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.external.AwsS3Service;
import br.finax.models.Account;
import br.finax.models.Category;
import br.finax.models.CreditCard;
import br.finax.models.Release;
import br.finax.repository.ReleaseRepository;
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
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static br.finax.external.AwsS3Service.getS3FileName;
import static br.finax.utils.DateUtils.getFirstAndLastDayOfMonth;
import static br.finax.utils.FileUtils.*;
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
    public List<MonthlyRelease> getMonthlyReleases(final String monthYear) {
        final long userId = getAuthUser().getId();
        final FirstAndLastDate firstAndLastDate;
        try {
            firstAndLastDate = getFirstAndLastDayOfMonth(monthYear);
        } catch (DateTimeParseException ex) {
            throw new ServiceException(ErrorCategory.BAD_REQUEST, "Invalid date format");
        }

        final var releases = releaseRepository.findAllByUserAndDatesBetween(
                userId,
                firstAndLastDate.firstDay(),
                firstAndLastDate.lastDay()
        );

        return mapToMonthlyReleases(releases, userId);
    }

    @Transactional(readOnly = true)
    public CashFlowValues getValues() {
        return new CashFlowValues(
                accountService.getBasicList(true),
                categoryService.findAllActiveByUser(),
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
        release.setS3FileName(existingRelease.getS3FileName());
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
    public Release saveAttachment(long releaseId, final @NonNull MultipartFile attachment) {
        final Release release = findById(releaseId);

        checkPermission(release);

        final String fileExtension = getFileExtension(attachment);
        final String fileName = getS3FileName(releaseId, fileExtension);

        try {
            final byte[] compressedFile = compressFile(attachment);

            final File tempFile = convertByteArrayToFile(compressedFile, fileName);

            try {
                if (release.getS3FileName() != null) {
                    awsS3Service.updateS3File(
                            concatS3FolderPath(release.getS3FileName()),
                            concatS3FolderPath(fileName),
                            tempFile
                    );
                } else {
                    awsS3Service.uploadS3File(
                            concatS3FolderPath(fileName),
                            tempFile
                    );
                }
            } finally {
                var _ = tempFile.delete();
            }

            release.setS3FileName(fileName);
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

        awsS3Service.deleteS3File(concatS3FolderPath(release.getS3FileName()));

        release.setS3FileName(null);
        release.setAttachmentName(null);

        return releaseRepository.save(release);
    }

    @Transactional(readOnly = true)
    public byte[] getAttachment(long releaseId) {
        final Release release = findById(releaseId);

        checkPermission(release);

        return awsS3Service.getS3File(
                concatS3FolderPath(release.getS3FileName())
        );
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
    public HomeRevenueExpense getHomeBalances(long userId, LocalDate firstDt, LocalDate lastDt) {
        return releaseRepository.getHomeBalances(userId, firstDt, lastDt);
    }

    @Transactional(readOnly = true)
    public List<HomeUpcomingRelease> getPayableAndReceivableAccounts(long userId, LocalDate firstDt, LocalDate lastDt) {
        return releaseRepository.getPayableAndReceivableAccounts(userId, firstDt, lastDt);
    }

    @Transactional(readOnly = true)
    public List<Release> findReleasesForReleasesByCategoryReport(long id, LocalDate startDate, LocalDate endDate, ReleaseType releaseType) {
        return releaseRepository.getReleasesForReleasesByCategoryReport(id, startDate, endDate, releaseType);
    }

    @Transactional(readOnly = true)
    public List<Release> findReleasesForReleasesByAccountReport(long userId, LocalDate firstDt, LocalDate lastDt, ReleaseType releaseType) {
        return releaseRepository.getReleasesForReleasesByAccountReport(userId, firstDt, lastDt, releaseType);
    }

    @Transactional(readOnly = true)
    public List<MonthlyRelease> getByInvoice(long userId, long creditCardId, LocalDate firstDt, LocalDate lastDt) {
        final var releases = releaseRepository.findAllByUserAndCreditCardAndDatesBetween(userId, creditCardId, firstDt, lastDt);

        return mapToMonthlyReleases(releases, getAuthUser().getId());
    }

    @Transactional(readOnly = true)
    public List<Release> getReleasesForBalanceEvolution(long userId, LocalDate firstDt, LocalDate lastDt, Long accountId) {
        return releaseRepository.getReleasesForBalanceEvolution(userId, firstDt, lastDt, accountId);
    }

    private void checkPermission(final Release release) {
        if (release.getUserId() != getAuthUser().getId())
            throw new WithoutPermissionException();
    }

    private String concatS3FolderPath(String filename) {
        return S3FolderPath.RELEASE_ATTACHMENTS.getPath().concat(filename);
    }

    private List<MonthlyRelease> mapToMonthlyReleases(List<Release> releases, long userId) {
        var accounts = accountService.getByUser()
                .stream().collect(Collectors.toUnmodifiableMap(
                        Account::getId,
                        ac -> new MonthlyReleaseAccount(ac.getId(), ac.getName(), ac.isAddToCashFlow())
                ));

        var cards = creditCardService.getByUser(userId)
                .stream().collect(Collectors.toUnmodifiableMap(
                        CreditCard::getId,
                        cd -> new MonthlyReleaseCard(cd.getId(), cd.getName(), cd.getImage()))
                );

        var categories = categoryService.getByUser()
                .stream().collect(Collectors.toUnmodifiableMap(
                        Category::getId,
                        ctg -> new MonthlyReleaseCategory(ctg.getId(), ctg.getName(), ctg.getColor(), ctg.getIcon()))
                );

        return releases.stream().map(release -> {
            var account = release.getAccountId() != null ? accounts.get(release.getAccountId()) : null;
            var card = release.getCreditCardId() != null ? cards.get(release.getCreditCardId()) : null;
            var targetAccount = release.getTargetAccountId() != null ? accounts.get(release.getTargetAccountId()) : null;
            var category = release.getCategoryId() != null ? categories.get(release.getCategoryId()) : null;

            return new MonthlyRelease(
                    release.getId(),
                    release.getUserId(),
                    release.getType(),
                    release.getDescription(),
                    release.getAmount(),
                    release.getDate(),
                    release.getTime(),
                    release.isDone(),
                    account,
                    card,
                    targetAccount,
                    category,
                    release.getObservation(),
                    release.getS3FileName(),
                    release.getAttachmentName(),
                    release.getDuplicatedReleaseId(),
                    releaseRepository.isDuplicatedRelease(release.getId()),
                    release.isBalanceAdjustment()
            );
        }).toList();
    }
}
