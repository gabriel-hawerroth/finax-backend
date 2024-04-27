package br.finax.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "cash_flow")
public class CashFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(length = 50)
    private String description;

    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 1)
    private String type;

    @Column(nullable = false)
    private boolean done;

    @Column(name = "target_account_id")
    private Long targetAccountId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 5)
    private String time;

    @Column(length = 100)
    private String observation;

    private byte[] attachment;

    @Column(name = "attachment_name")
    private String attachmentName;

    @Column(name = "duplicated_release_id")
    private Long duplicatedReleaseId;

    @Column(length = 12)
    private String repeat;

    @Column(name = "fixed_by", length = 10)
    private String fixedBy;

    private Long credit_card_id;
}
