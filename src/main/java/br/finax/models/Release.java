package br.finax.models;

import br.finax.enums.release.ReleaseFixedby;
import br.finax.enums.release.ReleaseRepeat;
import br.finax.enums.release.ReleaseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "release")
public class Release {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private long userId;

    @Column(length = 50)
    private String description;

    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ReleaseType type;

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

    @Column(name = "attachment_s3_file_name")
    private String attachmentS3FileName;

    @Column(name = "attachment_name")
    private String attachmentName;

    @Column(name = "duplicated_release_id")
    private Long duplicatedReleaseId;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private ReleaseRepeat repeat;

    @Enumerated(EnumType.STRING)
    @Column(name = "fixed_by", updatable = false)
    private ReleaseFixedby fixedBy;

    @Column(name = "credit_card_id")
    private Long creditCardId;

    @Column(name = "is_balance_adjustment", nullable = false)
    private boolean isBalanceAdjustment;
}
