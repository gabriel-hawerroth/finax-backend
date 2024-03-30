package br.finax.models;

import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "credit_card")
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long user_id;

    private String name;

    private double card_limit;

    private int close_day;

    private int expires_day;

    private String image;

    private long standard_payment_account_id;

    private boolean active;
}
