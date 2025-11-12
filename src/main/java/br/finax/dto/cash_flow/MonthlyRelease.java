package br.finax.dto.cash_flow;

import br.finax.enums.release.ReleaseType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyRelease(
        long id,
        long userId,
        ReleaseType type,
        String description,
        BigDecimal amount,
        LocalDate date,
        String time,
        boolean done,
        MonthlyReleaseAccount account,
        MonthlyReleaseCard card,
        MonthlyReleaseAccount targetAccount,
        MonthlyReleaseCategory category,
        String observation,
        String attachmentS3FileName,
        String attachmentName,
        Long duplicatedReleaseId,
        boolean isDuplicatedRelease,
        boolean isBalanceAdjustment,
        Integer installmentNumber,
        Integer totalInstallments
) {
}
