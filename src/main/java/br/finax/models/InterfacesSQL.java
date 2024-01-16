package br.finax.models;

import java.util.Date;

public class InterfacesSQL {

    public interface MonthlyReleases {
        Long getId();
        Long getUserId();
        String getDescription();
        Long getAccountId();
        String getAccountName();
        Double getAmount();
        String getType();
        Boolean getDone();
        Long getTargetAccountId();
        String getTargetAccountName();
        Long getCategoryId();
        String getCategoryName();
        String getCategoryColor();
        String getCategoryIcon();
        Date getDate();
        String getTime();
        String getObservation();
        byte[] getAttachment();
        String getAttachmentName();
        Long getDuplicatedReleaseId();
        Boolean getIsDuplicatedRelease();
    }

    public interface MonthlyBalance {
        Double getRevenues();
        Double getExpenses();
        Double getBalance();
        Double getGeneralBalance();
        Double getExpectedBalance();
    }
}
