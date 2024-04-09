package br.finax.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "cash_flow")
public class CashFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private long userId;

    private String description;

    @Column(name = "account_id")
    private Long accountId;

    private double amount;

    private String type;

    private boolean done;

    @Column(name = "target_account_id")
    private Long targetAccountId;

    @Column(name = "category_id")
    private Long categoryId;

    private LocalDate date;

    private String time;

    private String observation;

    private byte[] attachment;

    @Column(name = "attachment_name")
    private String attachmentName;

    @Column(name = "duplicated_release_id")
    private Long duplicatedReleaseId;

    private String repeat;

    @Column(name = "fixed_by")
    private String fixedBy;

    private Long credit_card_id;
}
