package br.finax.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "invoice_payment")
public class InvoicePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_card_id", nullable = false, updatable = false)
    private long creditCardId;

    @NotBlank
    @Column(name = "month_year", nullable = false, length = 7)
    private String monthYear;

    @Column(name = "payment_account_id", nullable = false)
    private Long paymentAccountId;

    @Column(name = "payment_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "payment_hour", length = 5)
    private String paymentHour;

    private byte[] attachment;

    @Column(name = "attachment_name")
    private String attachmentName;
}
