package br.finax.dto;

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

        String getType();

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

        String getAttachment();

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

        long getUser_id();

        String getName();

        double getCard_limit();

        int getClose_day();

        int getExpires_day();

        String getImage();

        long getStandard_payment_account_id();

        boolean getActive();

        String getAccount_name();

        String getAccount_image();
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

        long getCredit_card_id();

        String getInvoice_month_year();

        long getPayment_account_id();

        String getPayment_account_name();

        String getPayment_account_image();

        double getPayment_amount();

        LocalDate getPayment_date();

        String getPayment_hour();

        String getAttachment_name();
    }
}
