package br.finax.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "bank_account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private long userId;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private boolean investments;

    @Column(name = "add_overall_balance", nullable = false)
    private boolean addOverallBalance;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean archived;

    @Column(length = 30)
    private String image;

    @Column(name = "account_number", length = 15)
    private String accountNumber;

    @Column(length = 5)
    private String agency;

    @Column(precision = 3)
    private Integer code;

    @Column(length = 2)
    private String type;
}
