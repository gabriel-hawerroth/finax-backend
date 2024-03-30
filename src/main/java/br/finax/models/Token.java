package br.finax.models;

import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id")
    private long userId;

    private String token;
}
