package br.finax.dto.cash_flow;

import br.finax.enums.release.ReleaseFixedby;
import br.finax.enums.release.ReleaseRepeat;
import br.finax.enums.release.ReleaseType;
import br.finax.models.Release;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaveReleaseDTO(
        String description,
        Long accountId,
        BigDecimal amount,
        ReleaseType type,
        boolean done,
        Long targetAccountId,
        Long categoryId,
        Long subcategoryId,
        LocalDate date,
        String time,
        String observation,
        String s3FileName,
        String attachmentName,
        Long duplicatedReleaseId,
        ReleaseRepeat repeat,
        ReleaseFixedby fixedBy,
        Long creditCardId,
        Boolean isBalanceAdjustment,
        Integer installmentNumber
) {

    public Release toEntity() {
        var release = new Release();
        release.setDescription(this.description);
        release.setAccountId(this.accountId);
        release.setAmount(this.amount);
        release.setType(this.type);
        release.setDone(this.done);
        release.setTargetAccountId(this.targetAccountId);
        release.setCategoryId(this.categoryId);
        release.setSubcategoryId(this.subcategoryId);
        release.setDate(this.date);
        release.setTime(this.time);
        release.setObservation(this.observation);
        release.setS3FileName(this.s3FileName);
        release.setAttachmentName(this.attachmentName);
        release.setDuplicatedReleaseId(this.duplicatedReleaseId);
        release.setRepeat(this.repeat);
        release.setFixedBy(this.fixedBy);
        release.setCreditCardId(this.creditCardId);
        release.setBalanceAdjustment(this.isBalanceAdjustment);
        release.setInstallmentNumber(this.installmentNumber);
        return release;
    }
}
