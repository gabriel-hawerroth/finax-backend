package br.finax.dto;

import br.finax.models.Release;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DuplicatedReleaseBuilder {
    private final Release duplicatedRelease;

    public DuplicatedReleaseBuilder(Release original) {
        // Copies the relevant properties from the original to the duplicate
        this.duplicatedRelease = new Release();
        duplicatedRelease.setUserId(original.getUserId());
        duplicatedRelease.setDescription(original.getDescription());
        duplicatedRelease.setAccountId(original.getAccountId());
        duplicatedRelease.setAmount(BigDecimal.ZERO);
        duplicatedRelease.setType(original.getType());
        duplicatedRelease.setDone(false);
        duplicatedRelease.setTargetAccountId(original.getTargetAccountId());
        duplicatedRelease.setCategoryId(original.getCategoryId());
        duplicatedRelease.setDate(null);
        duplicatedRelease.setTime(original.getTime());
        duplicatedRelease.setObservation(original.getObservation());
        duplicatedRelease.setAttachmentS3FileName(null);
        duplicatedRelease.setAttachmentName(null);
        duplicatedRelease.setDuplicatedReleaseId(original.getId());
        duplicatedRelease.setRepeat(original.getRepeat());
        duplicatedRelease.setFixedBy(original.getFixedBy());
        duplicatedRelease.setCreditCardId(original.getCreditCardId());
        duplicatedRelease.setBalanceAdjustment(original.isBalanceAdjustment());
    }

    public DuplicatedReleaseBuilder amount(BigDecimal amount) {
        duplicatedRelease.setAmount(amount);
        return this;
    }

    public DuplicatedReleaseBuilder date(LocalDate date) {
        duplicatedRelease.setDate(date);
        return this;
    }

    public Release build() {
        return duplicatedRelease;
    }
}
