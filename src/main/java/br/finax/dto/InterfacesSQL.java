package br.finax.dto;

import br.finax.models.Category;

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
        long getAccountId();
        String getAccountName();
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
        String getAttachmentName();
        Long getDuplicatedReleaseId();
        boolean getIsDuplicatedRelease();
        boolean getIsCreditCardRelease();
        String getCreditCardImg();
    }

    public interface MonthlyBalance {
        double getRevenues();
        double getExpenses();
        double getGeneralBalance();
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

    public interface SpendsByCategory {

    }
}
