package br.finax.finax.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "account_name")
    private String accountName;

    private Double balance;

    private boolean investments;

    @Column(name = "add_overall_balance")
    private boolean addOverallBalance;

    private boolean active;

    private boolean archived;

    @Column(name = "presentation_sequence")
    private Integer presentationSequence;
}
