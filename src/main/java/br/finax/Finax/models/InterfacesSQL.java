package br.finax.finax.models;

import java.sql.Time;
import java.util.Date;

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
        Date getDate();
        String getTime();
        String getObservation();
    }
}
