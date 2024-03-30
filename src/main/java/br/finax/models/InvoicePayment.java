package br.finax.models;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "invoice_payment")
public class InvoicePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id")
    private long invoiceId;

    @Column(name = "payment_account_id")
    private long paymentAccountId;

    @Column(name = "payment_amount")
    private double paymentAmount;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_hour")
    private String paymentHour;

    private byte[] attachment;

    @Column(name = "attachment_name")
    private String attachmentName;
}
