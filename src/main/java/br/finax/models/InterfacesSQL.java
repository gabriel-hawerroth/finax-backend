package br.finax.models;

import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

public class InterfacesSQL {

    public interface MonthlyCashFlow {
        Long getId();
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
    }

    public interface MonthlyValues {
        Double getRevenues();
        Double getExpenses();
    }
}
