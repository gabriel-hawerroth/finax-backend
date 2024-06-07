package br.finax.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private long userId;

    @NotBlank
    @Column(nullable = false, length = 40)
    private String name;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String color;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String icon;

    @NotBlank
    @Column(nullable = false, length = 1, updatable = false)
    private String type;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean essential;
}
