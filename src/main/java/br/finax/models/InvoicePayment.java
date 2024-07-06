package br.finax.models;

import jakarta.persistence.*;
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

    @Column(nullable = false, updatable = false)
    private long credit_card_id;

    @NotBlank
    @Column(nullable = false, length = 7)
    private String month_year;

    @Column(nullable = false)
    private Long payment_account_id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal payment_amount;

    @Column(nullable = false)
    private LocalDate payment_date;

    @Column(length = 5)
    private String payment_hour;

    private byte[] attachment;

    private String attachment_name;
}
