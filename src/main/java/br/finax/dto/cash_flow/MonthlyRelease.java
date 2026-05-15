package br.finax.dto.cash_flow;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import br.finax.enums.release.ReleaseType;

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
        MonthlyReleaseCategory subcategory,
        String observation,
        String attachmentS3FileName,
        String attachmentName,
        Long duplicatedReleaseId,
        boolean isDuplicatedRelease,
        boolean isBalanceAdjustment,
        Integer installmentNumber,
        Integer totalInstallments,
        Instant createdAt,
        Instant updatedAt
) {
}
