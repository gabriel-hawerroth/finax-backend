package br.finax.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "bank_accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id")
    private long userId;

    private String name;

    private double balance;

    private boolean investments;

    @Column(name = "add_overall_balance")
    private boolean addOverallBalance;

    private boolean active;

    private boolean archived;

    private String image;

    @Column(name = "account_number")
    private String accountNumber;

    private Integer agency;

    private Integer code;

    private String type;
}
