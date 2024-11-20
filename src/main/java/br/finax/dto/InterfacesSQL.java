package br.finax.dto;

import br.finax.enums.AccountType;
import br.finax.enums.release.ReleaseType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@SuppressWarnings("unused")
public class InterfacesSQL {

    public interface GenericIdDs {
        long getId();

        String getDs();
    }

    public interface MonthlyRelease {
        long getId();

        long getUserId();

        String getDescription();

        Long getAccountId();

        String getAccountName();

        Long getCardId();

        String getCardName();

        String getCardImg();

        BigDecimal getAmount();

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

    public interface HomeRevenueExpense {
        BigDecimal getRevenues();

        BigDecimal getExpenses();
    }

    public interface UserCreditCard {
        long getId();

        long getUserId();

        String getName();

        BigDecimal getCardLimit();

        int getCloseDay();

        int getExpiresDay();

        String getImage();

        long getStandardPaymentAccountId();

        boolean getActive();

        String getAccountName();

        String getAccountImage();
    }

    public interface BasicAccount {
        long getId();

        String getName();

        String getImage();

        BigDecimal getBalance();

        AccountType getType();
    }

    public interface BasicCard {
        long getId();

        String getName();

        String getImage();
    }

    public interface InvoicePaymentPerson {
        long getId();

        long getCreditCardId();

        String getMonthYear();

        BigDecimal getPaymentAmount();

        LocalDate getPaymentDate();

        String getPaymentHour();

        String getAttachmentName();

        long getPaymentAccountId();

        String getPaymentAccountName();

        String getPaymentAccountImage();
    }

    public interface HomeAccount {
        String getName();

        String getImage();

        BigDecimal getBalance();

        AccountType getType();
    }

    public interface HomeUpcomingRelease {
        String getCategoryColor();

        String getCategoryIcon();

        String getCategoryName();

        boolean getIsCreditCardRelease();

        String getDescription();

        String getAccountName();

        String getCreditCardName();

        Date getDate();

        BigDecimal getAmount();

        String getType();
    }
}
