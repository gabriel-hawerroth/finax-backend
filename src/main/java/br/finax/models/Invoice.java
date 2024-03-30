package br.finax.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long user_id;

    private long credit_card_id;

    private String month_year;

    private Long payment_account_id;

    private LocalDate payment_date;

    public Invoice(long user_id, long credit_card_id, String month_year) {
        this.user_id = user_id;
        this.credit_card_id = credit_card_id;
        this.month_year = month_year;
    }
}
