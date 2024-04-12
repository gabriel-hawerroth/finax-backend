package br.finax.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "invoice_payment")
public class InvoicePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long credit_card_id;

    @Column(length = 7, nullable = false)
    private String invoice_month_year;

    @Column(nullable = false)
    private Long payment_account_id;

    @Column(nullable = false, precision = 2)
    private double payment_amount;

    @Column(nullable = false)
    private LocalDate payment_date;

    private String payment_hour;

    private byte[] attachment;

    private String attachment_name;
}
