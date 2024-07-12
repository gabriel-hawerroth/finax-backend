package br.finax.dto;

import br.finax.enums.release.ReleaseType;

import java.time.LocalDate;
import java.util.Date;

@SuppressWarnings("unused")
public class InterfacesSQL {

    public interface GenericIdDs {
        long getId();

        String getDs();
    }

    public interface MonthlyReleases {
        long getId();

        long getUserId();

        String getDescription();

        Long getAccountId();

        String getAccountName();

        Long getCardId();

        String getCardName();

        String getCardImg();

        double getAmount();

        ReleaseType getType();

        boolean getDone();

        Long getTargetAccountId();

        String getTargetAccountName();

        Long getCategoryId();

        String getCategoryName();

        String getCategoryColor();

        String getCategoryIcon();

        Date getDate();

        String getTime();

        String getObservation();

        String getAttachmentS3FileName();

        String getAttachmentName();

        Long getDuplicatedReleaseId();

        boolean getIsDuplicatedRelease();

        boolean getIsBalanceAdjustment();
    }

    public interface HomeBalances {
        double getRevenues();

        double getExpenses();
    }

    public interface UserCreditCards {
        long getId();

        long getUserId();

        String getName();

        double getCardLimit();

        int getCloseDay();

        int getExpiresDay();

        String getImage();

        long getStandardPaymentAccountId();

        boolean getActive();

        String getAccountName();

        String getAccountImage();
    }

    public interface AccountBasicList {
        long getId();

        String getName();

        String getImage();

        long getBalance();
    }

    public interface CardBasicList {
        long getId();

        String getName();

        String getImage();
    }

    public interface InvoicePaymentsPerson {
        long getId();

        long getCreditCardId();

        String getMonthYear();

        double getPaymentAmount();

        LocalDate getPaymentDate();

        String getPaymentHour();

        String getAttachmentName();

        long getPaymentAccountId();

        String getPaymentAccountName();

        String getPaymentAccountImage();
    }
}
