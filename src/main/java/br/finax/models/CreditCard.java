package br.finax.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "credit_card")
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long user_id;

    private String name;

    private Double card_limit;

    private int close_day;

    private int expires_day;

    private String image;

    private Long standard_payment_account_id;

    private boolean active;
}
