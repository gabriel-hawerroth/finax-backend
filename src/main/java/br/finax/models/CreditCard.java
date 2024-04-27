package br.finax.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "credit_card")
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private long user_id;

    @NotBlank
    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal card_limit;

    @Min(1)
    @Max(31)
    private int close_day;

    @Min(1)
    @Max(31)
    private int expires_day;

    @Column(length = 25)
    private String image;

    @Column(nullable = false)
    private long standard_payment_account_id;

    @Column(nullable = false)
    private boolean active;
}
