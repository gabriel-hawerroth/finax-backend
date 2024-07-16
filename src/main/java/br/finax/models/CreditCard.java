package br.finax.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @NotBlank
    @Column(nullable = false, length = 40)
    private String name;

    @Column(name = "card_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal cardLimit;

    @Min(1)
    @Max(31)
    @Column(name = "close_day")
    private int closeDay;

    @Min(1)
    @Max(31)
    @Column(name = "expires_day")
    private int expiresDay;

    @Column(length = 25)
    private String image;

    @Column(name = "standard_payment_account_id", nullable = false)
    private long standardPaymentAccountId;

    @Column(nullable = false)
    private boolean active;
}
