package br.finax.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private long userId;

    @Column(nullable = false, length = 64)
    private String token;
}
