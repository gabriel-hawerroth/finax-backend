package br.finax.models;

import br.finax.enums.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private long userId;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2, columnDefinition = "numeric(15,2) default 0.00")
    private BigDecimal balance;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean investments;

    @Column(name = "add_overall_balance", nullable = false, columnDefinition = "boolean default true")
    private boolean addOverallBalance;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active;

    @Column(length = 30)
    private String image;

    @Column(name = "account_number", length = 15)
    private String accountNumber;

    @Column(length = 5)
    private String agency;

    @Column(precision = 3)
    private Integer code;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    @Column(name = "primary_account_id")
    private Long primaryAccountId;

    @Column(name = "add_to_cash_flow", nullable = false, columnDefinition = "boolean default true")
    private boolean addToCashFlow;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean grouper;
}
