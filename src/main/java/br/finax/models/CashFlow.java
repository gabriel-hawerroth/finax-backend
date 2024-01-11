package br.finax.models;

import lombok.Data;

import javax.persistence.*;
import java.sql.Time;
import java.util.Date;

@Data
@Entity
@Table(name = "cash_flow")
public class CashFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Column(name = "account_id")
    private Long accountId;

    private Double amount;

    private String type;

    private boolean done;

    @Column(name = "target_account_id")
    private Long targetAccountId;

    @Column(name = "category_id")
    private Long categoryId;

    private Date date;

    private String time;

    private String observation;

    private byte[] attachment;

    @Column(name = "attachment_name")
    private String attachmentName;
}
